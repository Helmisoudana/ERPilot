#!/usr/bin/env bash


set -uo pipefail

API_URL="${ERPILOT_URL:-http://localhost:8080}/api/query"

# ── Couleurs ──────────────────────────────────────────────────────────────
RESET='\033[0m'
BOLD='\033[1m'
DIM='\033[2m'
CYAN='\033[38;5;51m'
MAGENTA='\033[38;5;213m'
GREEN='\033[38;5;120m'
YELLOW='\033[38;5;220m'
RED='\033[38;5;203m'
BLUE='\033[38;5;75m'
GRAY='\033[38;5;244m'

# ── Vérification des dépendances ─────────────────────────────────────────
if ! command -v jq >/dev/null 2>&1; then
    echo -e "${RED}✗ 'jq' est requis mais introuvable.${RESET}"
    echo -e "${GRAY}  Installe-le avec : sudo apt install -y jq${RESET}"
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    echo -e "${RED}✗ 'curl' est requis mais introuvable.${RESET}"
    exit 1
fi

# ── Bannière ──────────────────────────────────────────────────────────────
clear
echo -e "${MAGENTA}${BOLD}"
cat << "EOF"
 ███████╗██████╗ ██████╗ ██╗██╗      ██████╗ ████████╗
 ██╔════╝██╔══██╗██╔══██╗██║██║     ██╔═══██╗╚══██╔══╝
 █████╗  ██████╔╝██████╔╝██║██║     ██║   ██║   ██║
 ██╔══╝  ██╔══██╗██╔═══╝ ██║██║     ██║   ██║   ██║
 ███████╗██║  ██║██║     ██║███████╗╚██████╔╝   ██║
 ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝ ╚═════╝    ╚═╝
EOF
echo -e "${RESET}${CYAN}${BOLD}     Interroge ton ERP en langage naturel${RESET}"
echo -e "${GRAY}     API : ${API_URL}${RESET}"
echo -e "${GRAY}     Tape ta question, ou 'exit' / 'quit' pour sortir.${RESET}"
echo

# ── Boucle principale ─────────────────────────────────────────────────────
while true; do
    echo -ne "${GREEN}${BOLD}❯ ${RESET}"
    read -r question

    [[ -z "$question" ]] && continue
    if [[ "$question" == "exit" || "$question" == "quit" ]]; then
        echo -e "${MAGENTA}À bientôt !${RESET}"
        break
    fi

    # Construction sûre du JSON (échappe correctement la question)
    payload=$(jq -n --arg q "$question" '{question: $q, role: "user"}')

    echo -ne "${DIM}${GRAY}  ⏳ Génération en cours...${RESET}"

    http_response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -d "$payload")

    http_code=$(echo "$http_response" | tail -n1)
    body=$(echo "$http_response" | sed '$d')

    # Efface la ligne "Génération en cours..."
    echo -ne "\r\033[K"

    if [[ -z "$body" ]]; then
        echo -e "${RED}✗ Pas de réponse du serveur (est-il bien lancé sur ${API_URL} ?)${RESET}"
        echo
        continue
    fi

    if [[ "$http_code" -ge 400 ]]; then
        message=$(echo "$body" | jq -r '.message // "Erreur inconnue"' 2>/dev/null)
        trace_id=$(echo "$body" | jq -r '.traceId // empty' 2>/dev/null)
        echo -e "${RED}${BOLD}✗ Erreur (HTTP $http_code)${RESET}"
        echo -e "${RED}  $message${RESET}"
        [[ -n "$trace_id" ]] && echo -e "${GRAY}  traceId: $trace_id${RESET}"
        echo
        continue
    fi

    # ── Affichage du SQL généré ──────────────────────────────────────────
    sql=$(echo "$body" | jq -r '.sqlExecute // .sqlGenere // empty')
    corrige=$(echo "$body" | jq -r '.corrige // false')
    dialecte=$(echo "$body" | jq -r '.dialecte // "?"')
    duree=$(echo "$body" | jq -r '.dureeExecutionMs // "?"')
    nb_lignes=$(echo "$body" | jq -r '.nombreLignes // 0')

    if [[ -n "$sql" ]]; then
        echo -e "${BLUE}${BOLD}  SQL${RESET} ${GRAY}(${dialecte}$( [[ "$corrige" == "true" ]] && echo ", auto-corrigé" ))${RESET}"
        echo -e "${BLUE}  ${sql}${RESET}"
        echo
    fi

    # ── Affichage des résultats sous forme de tableau ────────────────────
    row_count=$(echo "$body" | jq '.lignes | length' 2>/dev/null)
    if [[ "$row_count" -gt 0 ]]; then
        echo -e "${YELLOW}${BOLD}  Résultats${RESET} ${GRAY}(${nb_lignes} ligne(s), ${duree} ms)${RESET}"

        table_tsv=$(
            {
                echo "$body" | jq -r '.colonnes | join("\t")'
                echo "$body" | jq -r '(.colonnes) as $cols | .lignes[] | [$cols[] as $c | (.[$c] | tostring)] | join("\t")'
            }
        )

        if command -v column >/dev/null 2>&1; then
            echo "$table_tsv" | column -t -s $'\t' | while IFS= read -r line; do
                echo -e "${GREEN}  ${line}${RESET}"
            done
        else
            echo "$table_tsv" | while IFS= read -r line; do
                echo -e "${GREEN}  ${line}${RESET}"
            done
        fi
    else
        echo -e "${GRAY}  Aucun résultat.${RESET}"
    fi

    echo
done

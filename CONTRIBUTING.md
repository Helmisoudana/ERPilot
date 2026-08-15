# Guide de contribution à ErPilot

Merci de l'intérêt que vous portez au projet **ErPilot** ! Les contributions sont grandement appréciées.

## 🐛 Signaler un bug
Si vous trouvez un dysfonctionnement :
1. Vérifiez dans les *Issues* existantes si le problème n'a pas déjà été signalé.
2. Si ce n'est pas le cas, ouvrez une nouvelle *Issue* en décrivant :
    - Le comportement attendu.
    - Le comportement observé.
    - Les étapes pour reproduire le problème.

## ✨ Proposer une fonctionnalité
Pour suggérer une amélioration ou une nouvelle fonctionnalité :
1. Ouvrez une *Issue* de type "Feature Request" pour en discuter avant de commencer à coder.
2. Expliquez clairement le besoin et les cas d'usage.

## 🛠️ Soumettre une modification (Pull Request)
1. **Forkez** le dépôt.
2. Créez une branche pour votre fonctionnalité : `git checkout -b feature/ma-fonctionnalite`.
3. Assurez-vous que le projet compile et que les tests passent : `mvn clean test`.
4. Effectuez vos commits avec des messages clairs.
5. Poussez votre branche : `git push origin feature/ma-fonctionnalite`.
6. Ouvrez une **Pull Request** sur le projet principal.

## 📏 Règles de code
- Respectez la structure Java / Spring Boot existante.
- Documentez les nouvelles méthodes/classes si nécessaire.
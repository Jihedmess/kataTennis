# kataTennis

Kata Tennis en Java 21 + Spring Boot.
L'ensemble du code source et des evolutions est disponible sur la branche `develop`.

Le service prend une sequence de points (lettres) et renvoie le score apres chaque point.
La sequence accepte n'importe quelles lettres (par exemple `A/B`, `C/D`, `X/Y`) avec une regle stricte:
elle doit contenir exactement 2 lettres distinctes, correspondant aux 2 joueurs du match.
Une sequence qui depasse la fin logique du match est rejetee avec une erreur metier claire.

## Prerequis

- Java 21
- Maven Wrapper (deja dans le repo)

## Lancer l'application


```bash
./mvnw spring-boot:run
```

## Lancer les tests

```bash
./mvnw test
```


## CI

Une pipeline CI GitHub Actions est ajoutee et execute `mvn clean verify` :
- sur chaque `push` vers `feature/**` et `bugfix/**`
- sur chaque `pull_request` vers `develop`, `main` et `release/**`

## API

### Endpoint

`POST /api/tennis/play`

### Requete

```json
{
  "sequence": "ABABAA"
}
```

### Reponse

```json
{
  "results": [
    "Player A : 15 / Player B : 0",
    "Player A : 15 / Player B : 15",
    "Player A : 30 / Player B : 15",
    "Player A : 30 / Player B : 30",
    "Player A : 40 / Player B : 30",
    "Player A wins the game"
  ]
}
```

## Gestion des erreurs

Exemple d'erreur de validation:

```json
{
  "code": "VALIDATION_ERROR",
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "sequence",
      "message": "sequence must contain only letters"
    }
  ],
  "path": "/api/tennis/play",
  "timestamp": "2026-03-23T12:00:00Z",
  "correlationId": "corr-123"
}
```

Exemple d'erreur JSON mal forme:

```json
{
  "code": "MALFORMED_JSON",
  "status": 400,
  "message": "Request body is invalid or unreadable",
  "errors": [
    {
      "field": "body",
      "message": "Invalid JSON payload"
    }
  ],
  "path": "/api/tennis/play",
  "timestamp": "2026-03-23T12:00:00Z",
  "correlationId": "corr-123"
}
```

Exemple d'erreur metier (sequence invalide dans les regles du jeu):

```json
{
  "code": "INVALID_GAME_SEQUENCE",
  "status": 400,
  "message": "Invalid sequence: additional points found after the game is already finished.",
  "errors": [
    {
      "field": "sequence",
      "message": "Invalid sequence: additional points found after the game is already finished."
    }
  ],
  "path": "/api/tennis/play",
  "timestamp": "2026-03-23T12:00:00Z",
  "correlationId": "corr-123"
}
```

Exemple d'erreur de commande (nombre de joueurs invalide):

```json
{
  "code": "INVALID_COMMAND",
  "status": 400,
  "message": "Invalid command: sequence must contain exactly 2 distinct players. Found 3.",
  "errors": [],
  "path": "/api/tennis/play",
  "timestamp": "2026-03-23T12:00:00Z",
  "correlationId": "corr-123"
}
```

Exemple d'erreur serveur:

```json
{
  "code": "INTERNAL_ERROR",
  "status": 500,
  "message": "Unexpected server error. Retry later or contact support with the correlationId.",
  "errors": [],
  "path": "/api/tennis/play",
  "timestamp": "2026-03-23T12:00:00Z",
  "correlationId": "corr-123"
}
```

## Correlation ID

- Header supporte: `X-Correlation-Id`
- Si absent, un id est genere automatiquement
- Si invalide (vide apres nettoyage) ou trop long, un id est regenere automatiquement
- L'id est renvoye dans la reponse et dans les logs

## Strategie de validation

- `infrastructure` : validation technique HTTP (JSON valide, champ requis, lettres uniquement)
- `application` : orchestration et normalisation de sequence (exactement 2 joueurs distincts)
- `domain` : invariants metier (regles de score, deuce/avantage, fin de partie)

## Note metier

Cette logique represente un cas reel de match de tennis:
- un match est joue par exactement 2 joueurs
- toute sequence invalide sur le nombre de joueurs est rejetee
- toute sequence qui continue apres la fin du match declenche une exception metier explicite


## Conventions de packages

- `domain` : logique metier pure (aucune dependance Spring)
- `application` : use cases, commandes, resultats, ports
- `infrastructure.config` : wiring Spring et creation des beans
- `infrastructure.adapter.in.rest.controller` : endpoints REST
- `infrastructure.adapter.in.rest.common` : constantes et utilitaires REST partages
- `infrastructure.adapter.in.rest.filter` : filtres HTTP
- `infrastructure.adapter.in.rest.exception` : gestion des exceptions HTTP
- `infrastructure.adapter.in.rest.mapper` : mapping use case -> DTO REST
- `infrastructure.adapter.in.rest.dto` : contrats d'entree/sortie HTTP
# XaltarStockAddon-JustRTP

Addon de **stock de RTP gratuits** pour le plugin [JustRTP](https://github.com/kotori2/justRTP).  
Permet aux joueurs d'accumuler et d'utiliser des téléportations gratuites, de s'en envoyer entre eux, et offre une gestion complète via commandes staff.

---

## Fonctionnement

- Chaque joueur dispose d'un **stock de RTP gratuits** avec une **limite maximale** définie par permission.
- Lors de sa **première connexion**, le joueur reçoit un bonus de RTP gratuits (configurable).
- À chaque `/rtp`, si le joueur possède des RTP gratuits, **un est consommé automatiquement** à la place du coût normal.
- Les joueurs peuvent **s'envoyer des RTP** entre eux avec `/rtpgive` (soumis à un cooldown configurable).
- Les administrateurs peuvent **gérer les stocks** avec `/rtpstaff`.

---

## Installation

1. Télécharge `JustRTP` (3.5+) et place-le dans `plugins/`.
2. Télécharge `XaltarStockAddon-JustRTP.jar` et place-le dans `plugins/JustRTP/addons/`.
3. Redémarre le serveur.
4. Configure `plugins/JustRTP/addons/XaltarStockAddon-JustRTP/config.yml` selon tes besoins.

### Dépendances

| Plugin | Requis | Description |
|--------|--------|-------------|
| Paper 1.21+ / Folia | ✅ Oui | Serveur Minecraft |
| JustRTP 3.5+ | ✅ Oui | Plugin parent |
| PlaceholderAPI | ❌ Optionnel | Pour les placeholders `%justrtpstock_*%` |

---

## Commandes

### Joueur

| Commande | Permission | Description |
|----------|------------|-------------|
| `/rtpstock` | `justrtp.stock.use` | Affiche ton stock de RTP gratuits |
| `/rtpgive <nombre> <joueur>` | `justrtp.stock.give` | Envoie des RTP gratuits à un joueur |

### Staff & Console

| Commande | Permission | Description |
|----------|------------|-------------|
| `/rtpstaff stock <joueur>` | `justrtp.stock.admin` | Voir le stock d'un joueur |
| `/rtpstaff add <nombre> <joueur>` | `justrtp.stock.admin` | Ajoute des RTP à un joueur |
| `/rtpstaff remove <nombre> <joueur>` | `justrtp.stock.admin` | Retire des RTP à un joueur |
| `/rtpstaff set <nombre> <joueur>` | `justrtp.stock.admin` | Définit le stock d'un joueur |

> **Note :** Les commandes `/rtpstaff` fonctionnent depuis la **console** et sont donc parfaites pour les **vouchers**.

---

## Permissions

| Permission | Description | Défaut |
|------------|-------------|--------|
| `justrtp.stock.use` | Utiliser `/rtpstock` | `true` |
| `justrtp.stock.give` | Utiliser `/rtpgive` | `true` |
| `justrtp.stock.admin` | Utiliser `/rtpstaff` | `op` |
| `justrtp.stock.default` | Limite par défaut (5) | `true` |
| `justrtp.stock.vip` | Limite VIP (10) | `false` |
| `justrtp.stock.mvp` | Limite MVP (20) | `false` |

Les limites sont entièrement configurables dans `config.yml`.

---

## Placeholders (PlaceholderAPI)

| Placeholder | Description |
|-------------|-------------|
| `%justrtpstock_stock%` | Stock actuel de RTP gratuits |
| `%justrtpstock_limit%` | Limite maximale du joueur |
| `%justrtpstock_remaining%` | RTP restants (`limit - stock`) |

---

## Utilisation avec des Vouchers

Pour créer un voucher qui donne des RTP gratuits à un joueur, utilise la commande :

```
/rtpstaff add <nombre> <joueur>
```

### Exemples de plugins vouchers

- **DeluxeMenus** : mets la commande dans l'action d'un item.
- **ItemJoin** : attribue un item avec une commande attachée.
- **Voucher** (ou tout plugin similaire) : utilise `%player%` comme variable joueur.

**Exemple de commande voucher :**
```
/rtpstaff add 5 %player%
```
Cela ajoutera 5 RTP gratuits au joueur qui utilise le voucher.

---

## Configuration (`config.yml`)

```yaml
# Langue des messages (fr / en)
language: fr

# Cooldown entre chaque envoi de RTP (en secondes)
cooldown-seconds: 5

# Nombre de RTP offerts à la première connexion
first-join-amount: 2

# Limites de stock par permission
limits:
  justrtp.stock.default: 5
  justrtp.stock.vip: 10
  justrtp.stock.mvp: 20
```

---

## Fichiers de langue

Les messages se trouvent dans :
- `plugins/JustRTP/addons/XaltarStockAddon-JustRTP/lang/fr.yml`
- `plugins/JustRTP/addons/XaltarStockAddon-JustRTP/lang/en.yml`

Tu peux les modifier et même créer de nouvelles langues.

---

## License

Développé par **RitchiQc** — Addon pour JustRTP.

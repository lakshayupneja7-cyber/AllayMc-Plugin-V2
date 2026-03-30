# AllayMc Exile Plugin

AllayMc Exile is a custom punishment system for SMP servers built on Paper.

## Features

- Timed exile system
- Overworld and Nether donut-style borders
- Staff-guided exile case creation
- Separate exile inventory
- Exact-item payoff system
- Early payoff with `/payoff`
- Staff-only exile coordinates
- Case archive in `plugins/AllayMc/cases/`
- Player history archive in `plugins/AllayMc/history/`
- Paid item claim system for staff
- Discord webhook support

## Commands

### Player
- `/exile`
- `/payoff`

### Staff
- `/exilecase create <player>`
- `/exilefree <player>`
- `/exileextend <player> <time>`
- `/exileremove <player>`
- `/exilecount <player>`
- `/serverborder`
- `/caseadmin info <caseId>`
- `/payoff claim <caseId>`

## Guided case creation

1. Run:
   - `/exilecase create <player>`
2. Enter in chat:
   - time
   - reason
   - restitution items
3. Confirm in GUI

Items format:
- `DIAMOND:32,IRON_INGOT:64`

## Payoff

Players can repay early with:
- `/payoff`

If exile timer ends, payoff becomes mandatory.

Grace timeout:
- 60 seconds

If player fails to pay exact required items:
- banned
- kicked

## Build

This project uses Maven and GitHub Actions.

### Local build
```bash
mvn clean package

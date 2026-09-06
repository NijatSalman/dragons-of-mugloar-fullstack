import { Button, Paper, Stack, Typography } from '@mui/material'
import { useGame } from '../state/useGame'

/** Shown while no game is running: explains the game and starts one. */
export function StartPanel() {
  const { state, actions } = useGame()

  return (
    <Paper sx={{ p: 3 }}>
      <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
        <Typography variant="h2" component="h2">
          Ready to train a dragon?
        </Typography>
        <Typography>
          The good folk of Mugloar leave notes for anyone with a dragon: mugs to fix, chickens to find, wagons to
          escort. You start with three lives and no gold. Take the notes worth taking, earn gold and score, and
          spend the gold on potions and upgrades in the shop.
        </Typography>
        <Button variant="contained" size="large" onClick={() => void actions.startGame()} disabled={state.busy}>
          Start new game
        </Button>
      </Stack>
    </Paper>
  )
}

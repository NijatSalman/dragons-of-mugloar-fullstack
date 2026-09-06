import { Button, Paper, Stack, Typography } from '@mui/material'
import type { Game } from '../api/types'

/** Shown when the last life is gone: the final score and the way back to a new game. */
export function GameOverBanner({ game, onStartAgain }: { game: Game; onStartAgain: () => void }) {
  return (
    <Paper component="section" aria-label="Game over" sx={{ p: 3 }}>
      <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
        <Typography variant="h2" component="h2">
          Game over
        </Typography>
        <Typography>
          Your dragon retired after {game.turn} turns with a score of <strong>{game.score}</strong>.
        </Typography>
        <Button variant="contained" onClick={onStartAgain}>
          Start again
        </Button>
      </Stack>
    </Paper>
  )
}

import SmartToyIcon from '@mui/icons-material/SmartToy'
import {
  Button,
  Chip,
  CircularProgress,
  LinearProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import type { AutoplayGameProgress, AutoplaySession } from '../api/types'

export const MAX_GAMES = 20

interface AutoplayPanelProps {
  session?: AutoplaySession
  disabled: boolean
  onStart: (games: number) => void
}

/** Let the dragon play whole games by itself: choose how many, start, watch the scores come in. */
export function AutoplayPanel({ session, disabled, onStart }: AutoplayPanelProps) {
  const [games, setGames] = useState(3)
  const running = session?.status === 'RUNNING'
  const valid = Number.isInteger(games) && games >= 1 && games <= MAX_GAMES

  return (
    <Paper component="section" aria-label="Autoplay" sx={{ p: 2 }}>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1 }}>
        <SmartToyIcon />
        <Typography variant="h2" component="h2" sx={{ flex: 1 }}>
          Let the dragon play
        </Typography>
        <TextField
          type="number"
          size="small"
          label="Games"
          value={games}
          onChange={(event) => setGames(Number(event.target.value))}
          error={!valid}
          helperText={valid ? '' : `1 to ${MAX_GAMES}`}
          slotProps={{ htmlInput: { min: 1, max: MAX_GAMES, 'aria-label': 'Number of games' } }}
          sx={{ width: 110 }}
        />
        <Button variant="contained" onClick={() => onStart(games)} disabled={disabled || running || !valid}>
          Start autoplay
        </Button>
      </Stack>
      <Typography variant="body2" sx={{ mt: 1, color: 'rgba(43,33,24,0.7)' }}>
        Games run in parallel on the server; each one plays until its dragon runs out of lives.
      </Typography>
      {session && <SessionProgress session={session} />}
    </Paper>
  )
}

function SessionProgress({ session }: { session: AutoplaySession }) {
  return (
    <Stack spacing={1} sx={{ mt: 2 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1 }}>
        <Chip
          size="small"
          color={session.status === 'FINISHED' ? 'success' : 'primary'}
          icon={session.status === 'RUNNING' ? <CircularProgress size={12} color="inherit" /> : undefined}
          label={session.status === 'RUNNING' ? 'running, updating every 3 s' : 'finished'}
        />
        <Typography variant="body2">
          {session.finished} of {session.requested} games finished
        </Typography>
        {session.summary && (
          <Typography variant="body2" sx={{ ml: 'auto', fontWeight: 600 }}>
            scores: min {session.summary.min} · avg {session.summary.avg} · max {session.summary.max}
          </Typography>
        )}
      </Stack>
      <LinearProgress variant="determinate" value={(100 * session.finished) / session.requested} aria-label="Games finished" />
      <Table size="small" aria-label="Game progress">
        <TableHead>
          <TableRow>
            <TableCell>Game</TableCell>
            <TableCell>Status</TableCell>
            <TableCell align="right">Score</TableCell>
            <TableCell align="right">Turn</TableCell>
            <TableCell align="right">Lives</TableCell>
            <TableCell align="right">Gold</TableCell>
            <TableCell align="right">Level</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {session.games.map((game, index) => (
            <GameRow key={game.gameId ?? index} game={game} />
          ))}
        </TableBody>
      </Table>
    </Stack>
  )
}

function GameRow({ game }: { game: AutoplayGameProgress }) {
  const tone = game.status === 'FINISHED' ? 'success' : game.status === 'FAILED' ? 'error' : 'default'
  return (
    <TableRow>
      <TableCell>{game.gameId ?? 'starting…'}</TableCell>
      <TableCell>
        <Chip
          size="small"
          color={tone}
          icon={game.status === 'RUNNING' ? <CircularProgress size={12} color="inherit" /> : undefined}
          label={game.status.toLowerCase()}
          title={game.error}
        />
      </TableCell>
      <TableCell align="right">{game.score}</TableCell>
      <TableCell align="right">{game.turn}</TableCell>
      <TableCell align="right">{game.lives}</TableCell>
      <TableCell align="right">{game.gold}</TableCell>
      <TableCell align="right">{game.level}</TableCell>
    </TableRow>
  )
}

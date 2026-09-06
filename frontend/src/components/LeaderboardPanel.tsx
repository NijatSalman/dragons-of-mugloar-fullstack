import RefreshIcon from '@mui/icons-material/Refresh'
import { Chip, IconButton, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import type { GameSummary } from '../api/types'

interface LeaderboardPanelProps {
  games: GameSummary[]
  disabled: boolean
  onRefresh: () => void
}

/** Every game this backend has seen, by hand or by the bot, highest score first. */
export function LeaderboardPanel({ games, disabled, onRefresh }: LeaderboardPanelProps) {
  return (
    <Paper component="section" aria-label="Leaderboard" sx={{ p: 2 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
        <Typography variant="h2" component="h2" sx={{ flex: 1 }}>
          Leaderboard
        </Typography>
        <IconButton aria-label="Refresh leaderboard" onClick={onRefresh} disabled={disabled}>
          <RefreshIcon />
        </IconButton>
      </Stack>
      {games.length === 0 ? (
        <Typography>No games yet. Play one, or let the dragon play.</Typography>
      ) : (
        <Table size="small" aria-label="Games by score">
          <TableHead>
            <TableRow>
              <TableCell>#</TableCell>
              <TableCell>Game</TableCell>
              <TableCell>Played by</TableCell>
              <TableCell align="right">Score</TableCell>
              <TableCell align="right">Turns</TableCell>
              <TableCell align="right">Lives</TableCell>
              <TableCell align="right">Gold</TableCell>
              <TableCell align="right">Level</TableCell>
              <TableCell>State</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {games.map((game, index) => (
              <TableRow key={game.gameId}>
                <TableCell>{index + 1}</TableCell>
                <TableCell>{game.gameId}</TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" label={game.origin === 'AUTOPLAY' ? 'dragon' : 'you'} />
                </TableCell>
                <TableCell align="right" sx={{ fontWeight: 600 }}>{game.score}</TableCell>
                <TableCell align="right">{game.turn}</TableCell>
                <TableCell align="right">{game.lives}</TableCell>
                <TableCell align="right">{game.gold}</TableCell>
                <TableCell align="right">{game.level}</TableCell>
                <TableCell>
                  <Chip size="small" color={game.over ? 'default' : 'primary'} label={game.over ? 'finished' : 'running'} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}

import RefreshIcon from '@mui/icons-material/Refresh'
import { Chip, IconButton, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import type { GameSummary } from '../api/types'

interface LeaderboardPanelProps {
  games: GameSummary[]
  disabled: boolean
  onRefresh: () => void
}

/** The ten best finished games this backend has seen, played by hand or by the dragon, highest score first. */
export function LeaderboardPanel({ games, disabled, onRefresh }: LeaderboardPanelProps) {
  return (
    <Paper component="section" aria-label="Top scores" sx={{ p: 2 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
        <Typography variant="h2" component="h2" sx={{ flex: 1 }}>
          Top 10 scores
        </Typography>
        <IconButton aria-label="Refresh top scores" onClick={onRefresh} disabled={disabled}>
          <RefreshIcon />
        </IconButton>
      </Stack>
      <Typography variant="body2" sx={{ mb: 1, color: 'rgba(43,33,24,0.7)' }}>
        Finished games only, since this backend started.
      </Typography>
      {games.length === 0 ? (
        <Typography>No finished games yet. Play one to the end, or let the dragon play.</Typography>
      ) : (
        <Table size="small" aria-label="Games by score">
          <TableHead>
            <TableRow>
              <TableCell>#</TableCell>
              <TableCell>Game</TableCell>
              <TableCell>Played by</TableCell>
              <TableCell align="right">Score</TableCell>
              <TableCell align="right">Turns</TableCell>
              <TableCell align="right">Gold left</TableCell>
              <TableCell align="right">Level</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {games.map((game, index) => (
              <TableRow key={game.gameId}>
                <TableCell>{index + 1}</TableCell>
                <TableCell>{game.gameId}</TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" label={game.origin === 'AUTOPLAY' ? 'autoplay' : 'manual'} />
                </TableCell>
                <TableCell align="right" sx={{ fontWeight: 600 }}>{game.score}</TableCell>
                <TableCell align="right">{game.turn}</TableCell>
                <TableCell align="right">{game.gold}</TableCell>
                <TableCell align="right">{game.level}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  )
}

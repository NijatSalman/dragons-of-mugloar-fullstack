import FavoriteIcon from '@mui/icons-material/Favorite'
import PaidIcon from '@mui/icons-material/Paid'
import StarIcon from '@mui/icons-material/Star'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import LogoutIcon from '@mui/icons-material/Logout'
import { Button, Chip, Paper, Stack, Typography } from '@mui/material'
import type { Game } from '../api/types'

interface StatusBarProps {
  game: Game
  onLeave: () => void
}

/** The numbers a player watches: lives, gold, score, dragon level and turn; and the way out of the game. */
export function StatusBar({ game, onLeave }: StatusBarProps) {
  return (
    <Paper component="section" aria-label="Game status" sx={{ p: 2 }}>
      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', alignItems: 'center', rowGap: 1 }}>
        <Chip icon={<FavoriteIcon />} color={game.lives <= 1 ? 'error' : 'default'} label={`${game.lives} lives`} />
        <Chip icon={<PaidIcon />} label={`${game.gold} gold`} />
        <Chip icon={<StarIcon />} color="primary" label={`${game.score} score`} />
        <Chip icon={<TrendingUpIcon />} label={`level ${game.level}`} />
        <Chip variant="outlined" label={`turn ${game.turn}`} />
        <Typography variant="body2" sx={{ ml: 'auto', color: 'rgba(43,33,24,0.7)' }}>
          Game {game.gameId}
        </Typography>
        <Button
          size="small"
          variant="text"
          startIcon={<LogoutIcon />}
          onClick={onLeave}
          sx={{ color: 'rgba(43,33,24,0.75)', textTransform: 'none', fontFamily: '"Crimson Pro", Georgia, serif', fontWeight: 600 }}
        >
          Leave game
        </Button>
      </Stack>
    </Paper>
  )
}

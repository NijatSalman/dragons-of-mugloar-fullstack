import HourglassBottomIcon from '@mui/icons-material/HourglassBottom'
import PaidIcon from '@mui/icons-material/Paid'
import RecommendIcon from '@mui/icons-material/Recommend'
import { Button, Card, CardActions, CardContent, Chip, Stack, Typography } from '@mui/material'
import type { Ad } from '../api/types'
import { chanceTone, percent } from './chance'

interface AdCardProps {
  ad: Ad
  disabled: boolean
  onSolve: (adId: string) => void
}

/** One ad from the board: what it asks, what it pays, how likely it is, and the button to attempt it. */
export function AdCard({ ad, disabled, onSolve }: AdCardProps) {
  return (
    <Card component="article" aria-label={ad.message} sx={{ outline: ad.recommended ? '2px solid #7ea15a' : 'none' }}>
      <CardContent>
        <Typography sx={{ fontWeight: 600, mb: 1.5 }}>{ad.message}</Typography>
        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', rowGap: 1 }}>
          <Chip size="small" color={chanceTone(ad.successChance)} label={`${ad.probability} · ${percent(ad.successChance)}`} />
          <Chip size="small" icon={<PaidIcon />} label={`${ad.reward} gold`} />
          <Chip size="small" variant="outlined" label={`worth ${ad.expectedValue.toFixed(1)}`} />
          <Chip
            size="small"
            variant="outlined"
            icon={<HourglassBottomIcon />}
            label={`${ad.expiresIn} ${ad.expiresIn === 1 ? 'turn' : 'turns'} left`}
          />
          {ad.recommended && <Chip size="small" color="success" icon={<RecommendIcon />} label="recommended" />}
        </Stack>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Button variant="contained" onClick={() => onSolve(ad.adId)} disabled={disabled}>
          Solve
        </Button>
      </CardActions>
    </Card>
  )
}

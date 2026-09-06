import RefreshIcon from '@mui/icons-material/Refresh'
import {
  Box,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Select,
  Stack,
  Switch,
  Typography,
  type SelectChangeEvent,
} from '@mui/material'
import { useState } from 'react'
import type { Ad } from '../api/types'
import { AdCard } from './AdCard'

type SortKey = 'value' | 'reward' | 'chance' | 'expiry'

const SORTERS: Record<SortKey, (left: Ad, right: Ad) => number> = {
  value: (left, right) => right.expectedValue - left.expectedValue,
  reward: (left, right) => right.reward - left.reward,
  chance: (left, right) => right.successChance - left.successChance,
  expiry: (left, right) => left.expiresIn - right.expiresIn,
}

interface AdBoardProps {
  ads: Ad[]
  disabled: boolean
  onSolve: (adId: string) => void
  onRefresh: () => void
}

/** The message board: every ad as a card, sortable, optionally only the recommended ones. */
export function AdBoard({ ads, disabled, onSolve, onRefresh }: AdBoardProps) {
  const [sortKey, setSortKey] = useState<SortKey>('value')
  const [recommendedOnly, setRecommendedOnly] = useState(false)

  const shown = ads.filter((ad) => !recommendedOnly || ad.recommended).sort(SORTERS[sortKey])

  return (
    <Paper component="section" aria-label="Ads" sx={{ p: 2 }}>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1, mb: 2 }}>
        <Typography variant="h2" component="h2" sx={{ flex: 1 }}>
          Ads on the board
        </Typography>
        <Select
          size="small"
          value={sortKey}
          onChange={(event: SelectChangeEvent) => setSortKey(event.target.value as SortKey)}
          inputProps={{ 'aria-label': 'Sort ads by' }}
        >
          <MenuItem value="value">Best value first</MenuItem>
          <MenuItem value="reward">Highest reward first</MenuItem>
          <MenuItem value="chance">Safest first</MenuItem>
          <MenuItem value="expiry">Expiring soonest first</MenuItem>
        </Select>
        <FormControlLabel
          control={<Switch checked={recommendedOnly} onChange={(_event, checked) => setRecommendedOnly(checked)} />}
          label="Recommended only"
        />
        <IconButton aria-label="Refresh ads" onClick={onRefresh} disabled={disabled}>
          <RefreshIcon />
        </IconButton>
      </Stack>
      {shown.length === 0 ? (
        <Typography>No ads to show.</Typography>
      ) : (
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }}>
          {shown.map((ad) => (
            <AdCard key={ad.adId} ad={ad} disabled={disabled} onSolve={onSolve} />
          ))}
        </Box>
      )}
    </Paper>
  )
}

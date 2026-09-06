import PaidIcon from '@mui/icons-material/Paid'
import { Avatar, Button, List, ListItem, ListItemAvatar, ListItemText, Paper, Stack, Tooltip, Typography } from '@mui/material'
import type { ShopItem } from '../api/types'
import { shopItemLook } from './shopItemIcon'

interface ShopPanelProps {
  items: ShopItem[]
  gold: number
  disabled: boolean
  onBuy: (itemId: string) => void
}

/** The shop: what is for sale, what it does, what it costs, and whether you can afford it right now. */
export function ShopPanel({ items, gold, disabled, onBuy }: ShopPanelProps) {
  return (
    <Paper component="section" aria-label="Shop" sx={{ p: 2 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'baseline', mb: 1 }}>
        <Typography variant="h2" component="h2" sx={{ flex: 1 }}>
          Shop
        </Typography>
        <Typography variant="body2" sx={{ color: 'rgba(43,33,24,0.7)' }}>
          Buying costs one turn
        </Typography>
      </Stack>
      {items.length === 0 ? (
        <Typography>The shop is closed.</Typography>
      ) : (
        <List disablePadding>
          {items.map((item) => {
            const look = shopItemLook(item.itemId)
            const affordable = gold >= item.cost
            return (
              <ListItem key={item.itemId} disableGutters divider sx={{ gap: 1 }}>
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: '#2a221a', color: '#d4a53a' }}>{look.icon}</Avatar>
                </ListItemAvatar>
                <ListItemText primary={item.name} secondary={look.effect} slotProps={{ secondary: { sx: { color: 'rgba(43,33,24,0.7)' } } }} />
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <Typography sx={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <PaidIcon fontSize="small" /> {item.cost}
                  </Typography>
                  <Tooltip title={affordable ? '' : `You need ${item.cost - gold} more gold`}>
                    <span>
                      <Button variant="contained" size="small" disabled={disabled || !affordable} onClick={() => onBuy(item.itemId)}>
                        Buy
                      </Button>
                    </span>
                  </Tooltip>
                </Stack>
              </ListItem>
            )
          })}
        </List>
      )}
    </Paper>
  )
}

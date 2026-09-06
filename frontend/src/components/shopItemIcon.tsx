import AirIcon from '@mui/icons-material/Air'
import InventoryIcon from '@mui/icons-material/Inventory2'
import LocalFireDepartmentIcon from '@mui/icons-material/LocalFireDepartment'
import MenuBookIcon from '@mui/icons-material/MenuBook'
import PetsIcon from '@mui/icons-material/Pets'
import ScienceIcon from '@mui/icons-material/Science'
import ShieldIcon from '@mui/icons-material/Shield'
import type { ReactElement } from 'react'

/** One icon and one line of explanation per shop item id the game server uses. */
interface ItemLook {
  icon: ReactElement
  effect: string
}

const LOOKS: Record<string, ItemLook> = {
  hpot: { icon: <ScienceIcon />, effect: 'Restores one life' },
  cs: { icon: <PetsIcon />, effect: 'Sharper claws: dragon level +1' },
  ch: { icon: <PetsIcon />, effect: 'Honed claws: dragon level +1' },
  gas: { icon: <LocalFireDepartmentIcon />, effect: 'Hotter fire: dragon level +1' },
  rf: { icon: <LocalFireDepartmentIcon />, effect: 'Rocket fire: dragon level +1' },
  wax: { icon: <ShieldIcon />, effect: 'Copper plating: dragon level +1' },
  iron: { icon: <ShieldIcon />, effect: 'Iron plating: dragon level +1' },
  tricks: { icon: <MenuBookIcon />, effect: 'New tricks: dragon level +1' },
  mtrix: { icon: <MenuBookIcon />, effect: 'Mega tricks: dragon level +1' },
  wingpot: { icon: <AirIcon />, effect: 'Stronger wings: dragon level +1' },
  wingpotmax: { icon: <AirIcon />, effect: 'Awesome wings: dragon level +1' },
}

export function shopItemLook(itemId: string): ItemLook {
  return LOOKS[itemId] ?? { icon: <InventoryIcon />, effect: 'A curious item' }
}

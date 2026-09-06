import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { ShopItem } from '../api/types'
import { ShopPanel } from './ShopPanel'

const items: ShopItem[] = [
  { itemId: 'hpot', name: 'Healing potion', cost: 50 },
  { itemId: 'cs', name: 'Claw Sharpening', cost: 100 },
]

describe('ShopPanel', () => {
  it('showsEveryItemWithEffectAndCost', () => {
    render(<ShopPanel items={items} gold={120} disabled={false} onBuy={vi.fn()} />)

    expect(screen.getByText('Healing potion')).toBeInTheDocument()
    expect(screen.getByText('Restores one life')).toBeInTheDocument()
    expect(screen.getByText('50')).toBeInTheDocument()
    expect(screen.getByText('Claw Sharpening')).toBeInTheDocument()
  })

  it('buyButtonReportsTheItemId', async () => {
    const onBuy = vi.fn()
    render(<ShopPanel items={items} gold={120} disabled={false} onBuy={onBuy} />)

    await userEvent.click(within(screen.getByText('Healing potion').closest('li')!).getByRole('button', { name: 'Buy' }))

    expect(onBuy).toHaveBeenCalledWith('hpot')
  })

  it('buyIsDisabledForItemsThatCostMoreThanTheGold', () => {
    render(<ShopPanel items={items} gold={60} disabled={false} onBuy={vi.fn()} />)

    const [potionBuy, clawsBuy] = screen.getAllByRole('button', { name: 'Buy' })
    expect(potionBuy).toBeEnabled()
    expect(clawsBuy).toBeDisabled()
  })

  it('everyBuyIsDisabledWhileBusy', () => {
    render(<ShopPanel items={items} gold={1000} disabled onBuy={vi.fn()} />)

    screen.getAllByRole('button', { name: 'Buy' }).forEach((button) => expect(button).toBeDisabled())
  })

  it('showsAMessageWhenThereAreNoItems', () => {
    render(<ShopPanel items={[]} gold={0} disabled={false} onBuy={vi.fn()} />)

    expect(screen.getByText('The shop is closed.')).toBeInTheDocument()
  })
})

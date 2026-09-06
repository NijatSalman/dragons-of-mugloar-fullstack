import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { pieceOfCakeAd, playingWithFireAd, quiteLikelyAd } from '../test/fixtures'
import { AdBoard } from './AdBoard'

const ads = [pieceOfCakeAd, playingWithFireAd, quiteLikelyAd]

function shownTitles() {
  return screen.getAllByRole('article').map((card) => card.getAttribute('aria-label'))
}

describe('AdBoard', () => {
  it('sortsByExpectedValueByDefault', () => {
    render(<AdBoard ads={ads} disabled={false} onSolve={vi.fn()} onRefresh={vi.fn()} />)

    expect(shownTitles()).toEqual([playingWithFireAd.message, quiteLikelyAd.message, pieceOfCakeAd.message])
  })

  it('sortsSafestFirstWhenAsked', async () => {
    render(<AdBoard ads={ads} disabled={false} onSolve={vi.fn()} onRefresh={vi.fn()} />)

    await userEvent.click(screen.getByRole('combobox', { name: 'Sort ads by' }))
    await userEvent.click(screen.getByRole('option', { name: 'Safest first' }))

    expect(shownTitles()).toEqual([pieceOfCakeAd.message, quiteLikelyAd.message, playingWithFireAd.message])
  })

  it('recommendedOnlyHidesRiskyAds', async () => {
    render(<AdBoard ads={ads} disabled={false} onSolve={vi.fn()} onRefresh={vi.fn()} />)

    await userEvent.click(screen.getByLabelText('Recommended only'))

    expect(shownTitles()).not.toContain(playingWithFireAd.message)
    expect(shownTitles()).toHaveLength(2)
  })

  it('refreshButtonAsksForANewBoard', async () => {
    const onRefresh = vi.fn()
    render(<AdBoard ads={ads} disabled={false} onSolve={vi.fn()} onRefresh={onRefresh} />)

    await userEvent.click(screen.getByRole('button', { name: 'Refresh ads' }))

    expect(onRefresh).toHaveBeenCalledOnce()
  })

  it('solvingAnAdReportsItsId', async () => {
    const onSolve = vi.fn()
    render(<AdBoard ads={[quiteLikelyAd]} disabled={false} onSolve={onSolve} onRefresh={vi.fn()} />)

    await userEvent.click(within(screen.getByRole('article')).getByRole('button', { name: 'Solve' }))

    expect(onSolve).toHaveBeenCalledWith('DSAUBsXa')
  })

  it('explainsAnEmptyBoard', () => {
    render(<AdBoard ads={[]} disabled={false} onSolve={vi.fn()} onRefresh={vi.fn()} />)

    expect(screen.getByText(/board is empty/)).toBeInTheDocument()
  })

  it('explainsWhenTheFilterHidesEveryAd', async () => {
    render(<AdBoard ads={[playingWithFireAd]} disabled={false} onSolve={vi.fn()} onRefresh={vi.fn()} />)

    await userEvent.click(screen.getByLabelText('Recommended only'))

    expect(screen.getByText(/None of the current ads is recommended/)).toBeInTheDocument()
  })
})

import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { playingWithFireAd, quiteLikelyAd } from '../test/fixtures'
import { AdCard } from './AdCard'

describe('AdCard', () => {
  it('adCardShowsMessageOddsRewardValueExpiryAndRecommendation', () => {
    render(<AdCard ad={quiteLikelyAd} disabled={false} onSolve={vi.fn()} />)

    expect(screen.getByText(quiteLikelyAd.message)).toBeInTheDocument()
    expect(screen.getByText('Quite likely · 80%')).toBeInTheDocument()
    expect(screen.getByText('21 gold')).toBeInTheDocument()
    expect(screen.getByText('worth 16.8')).toBeInTheDocument()
    expect(screen.getByText('7 turns left')).toBeInTheDocument()
    expect(screen.getByText('recommended')).toBeInTheDocument()
  })

  it('adCardHidesTheRecommendationForARiskyAd', () => {
    render(<AdCard ad={playingWithFireAd} disabled={false} onSolve={vi.fn()} />)

    expect(screen.queryByText('recommended')).not.toBeInTheDocument()
    expect(screen.getByText('1 turn left')).toBeInTheDocument()
  })

  it('adCardSolveButtonReportsTheAdId', async () => {
    const onSolve = vi.fn()
    render(<AdCard ad={quiteLikelyAd} disabled={false} onSolve={onSolve} />)

    await userEvent.click(screen.getByRole('button', { name: 'Solve' }))

    expect(onSolve).toHaveBeenCalledWith('DSAUBsXa')
  })

  it('adCardSolveButtonIsDisabledWhileBusy', () => {
    render(<AdCard ad={quiteLikelyAd} disabled onSolve={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Solve' })).toBeDisabled()
  })
})

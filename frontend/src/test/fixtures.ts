import type { Ad, Game } from '../api/types'

/** Realistic test data: real ad texts and ids from the game server. */
export const runningGame: Game = {
  gameId: 'ggLmesXI',
  lives: 3,
  gold: 120,
  level: 1,
  score: 300,
  highScore: 300,
  turn: 15,
  over: false,
  origin: 'MANUAL',
}

export const finishedGame: Game = { ...runningGame, lives: 0, score: 5239, turn: 201, over: true }

export const quiteLikelyAd: Ad = {
  adId: 'DSAUBsXa',
  message: 'Help Majid Desprez to transport a magic beer mug to steppe in Falldean',
  reward: 21,
  expiresIn: 7,
  probability: 'Quite likely',
  successChance: 0.8,
  expectedValue: 16.8,
  recommended: true,
}

export const pieceOfCakeAd: Ad = {
  adId: 'HiCtYxHC',
  message: 'Help Praskoviya Richard to fix their beer mug',
  reward: 4,
  expiresIn: 3,
  probability: 'Piece of cake',
  successChance: 0.95,
  expectedValue: 3.8,
  recommended: true,
}

export const playingWithFireAd: Ad = {
  adId: '3haCbU60',
  message: 'Escort Gervase Wheeler to grassland in Frostdinny where they can meet with their long lost chicken',
  reward: 70,
  expiresIn: 1,
  probability: 'Playing with fire',
  successChance: 0.25,
  expectedValue: 17.5,
  recommended: false,
}

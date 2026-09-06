import { createTheme } from '@mui/material/styles'

/**
 * The one place for the look of the app: a dark, medieval mood. Charcoal and deep brown backgrounds, parchment
 * cards, gold for what matters, ember red only for danger. Headings in Cinzel, text in Crimson Pro.
 */
export const colors = {
  night: '#15110d',
  bark: '#2a221a',
  parchment: '#f1e4c8',
  parchmentDark: '#e2cfa6',
  ink: '#2b2118',
  gold: '#d4a53a',
  ember: '#b5441c',
  moss: '#5d7a3a',
  smoke: '#8d8274',
}

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: colors.gold, contrastText: colors.night },
    secondary: { main: colors.moss },
    error: { main: colors.ember },
    success: { main: '#7ea15a' },
    warning: { main: '#d98e2b' },
    background: { default: colors.night, paper: colors.parchment },
    text: { primary: colors.parchment, secondary: colors.smoke },
  },
  shape: { borderRadius: 6 },
  typography: {
    fontFamily: '"Crimson Pro", Georgia, serif',
    fontSize: 16,
    h1: { fontFamily: '"Cinzel", Georgia, serif', fontSize: '1.9rem', fontWeight: 700, letterSpacing: '0.06em' },
    h2: { fontFamily: '"Cinzel", Georgia, serif', fontSize: '1.3rem', fontWeight: 700, letterSpacing: '0.04em' },
    button: { fontFamily: '"Cinzel", Georgia, serif', fontWeight: 700, letterSpacing: '0.05em' },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: colors.night,
          backgroundImage:
            'radial-gradient(ellipse at top, rgba(212,165,58,0.12), transparent 55%),' +
            'radial-gradient(ellipse at bottom, rgba(181,68,28,0.18), transparent 60%),' +
            'repeating-linear-gradient(0deg, rgba(255,255,255,0.015) 0 2px, transparent 2px 4px)',
          backgroundAttachment: 'fixed',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: colors.bark,
          backgroundImage: 'linear-gradient(180deg, #3a2f23 0%, #2a221a 100%)',
          borderBottom: `2px solid ${colors.gold}`,
          boxShadow: '0 4px 18px rgba(0,0,0,0.6)',
        },
      },
    },
    // Cards read as parchment: light background with dark ink, a thin worn border and a soft shadow.
    MuiPaper: {
      styleOverrides: {
        root: {
          color: colors.ink,
          backgroundImage: 'linear-gradient(160deg, #f6ecd6 0%, #ecdcb7 100%)',
          border: `1px solid ${colors.parchmentDark}`,
          boxShadow: '0 6px 20px rgba(0,0,0,0.45)',
        },
      },
    },
    MuiButton: {
      variants: [
        {
          props: { variant: 'contained', color: 'primary' },
          style: {
            backgroundImage: 'linear-gradient(180deg, #e3b84e 0%, #c3932c 100%)',
            border: '1px solid #8c6a1c',
            '&:hover': { backgroundImage: 'linear-gradient(180deg, #edc45e 0%, #cf9e33 100%)' },
          },
        },
      ],
    },
    // Chips live on parchment cards, so they take ink colours, not the dark-mode defaults.
    MuiChip: {
      styleOverrides: {
        root: {
          fontFamily: '"Crimson Pro", Georgia, serif',
          fontWeight: 600,
          fontSize: '1rem',
          color: colors.ink,
          backgroundColor: 'rgba(43,33,24,0.08)',
          border: '1px solid rgba(43,33,24,0.25)',
        },
        icon: { color: 'inherit' },
        colorPrimary: { color: colors.night, backgroundColor: colors.gold, borderColor: '#8c6a1c' },
        colorError: { color: colors.parchment, backgroundColor: colors.ember, borderColor: '#7a2d12' },
        outlined: { backgroundColor: 'transparent', borderColor: 'rgba(43,33,24,0.45)' },
      },
    },
    MuiAlert: {
      styleOverrides: {
        standard: { color: colors.ink },
        filled: { color: colors.night, fontWeight: 600 },
      },
    },
    // Form controls also sit on parchment cards.
    MuiInputBase: {
      styleOverrides: {
        root: { color: colors.ink, fontFamily: '"Crimson Pro", Georgia, serif', fontWeight: 600 },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        notchedOutline: { borderColor: 'rgba(43,33,24,0.45)' },
      },
    },
    MuiSelect: {
      styleOverrides: {
        icon: { color: colors.ink },
      },
    },
    MuiFormControlLabel: {
      styleOverrides: {
        label: { color: colors.ink, fontWeight: 600 },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: { backgroundImage: 'none', backgroundColor: colors.parchment },
      },
    },
  },
})

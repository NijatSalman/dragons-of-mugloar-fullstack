import { createTheme } from '@mui/material/styles'

/** The one place for colours, spacing and typography; components take everything from here. */
export const theme = createTheme({
  palette: {
    primary: { main: '#b5441c' },
    secondary: { main: '#2f7d4a' },
    background: { default: '#f6f1e7', paper: '#ffffff' },
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    h1: { fontSize: '1.75rem', fontWeight: 600 },
    h2: { fontSize: '1.25rem', fontWeight: 600 },
  },
})

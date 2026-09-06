import { SvgIcon, type SvgIconProps } from '@mui/material'

/** Our own dragon silhouette for the title bar; drawn here, so no third-party artwork is used. */
export function DragonMark(props: SvgIconProps) {
  return (
    <SvgIcon viewBox="0 0 64 64" {...props}>
      <path d="M6 40c4-10 12-16 22-16 3 0 5 1 7 2l3-9 5 7 7-4-3 9c6 3 10 8 11 15l-8-3c1 4 0 8-3 11l-4-6c-3 2-6 3-10 3-2 4-6 6-11 6 2-3 3-6 3-9-8-1-14-3-19-6z" />
      <circle cx="38" cy="30" r="1.8" fill="#15110d" />
    </SvgIcon>
  )
}

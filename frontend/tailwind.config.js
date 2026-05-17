/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        amex: {
          blue:      '#006FCF',
          dark:      '#00539C',
          navy:      '#002663',
          light:     '#E8F3FC',
          gold:      '#B5A36A',
        },
        surface: {
          white:  '#FFFFFF',
          light:  '#F7F8FA',
          section:'#F0F4F9',
          border: '#D9DCE0',
          muted:  '#ECEEF1',
        },
        ink: {
          primary:   '#1A1A1A',
          secondary: '#53565A',
          muted:     '#8A8D91',
        }
      },
      fontFamily: {
        sans: ['"Helvetica Neue"', 'Arial', 'sans-serif'],
      },
      boxShadow: {
        'amex-sm':  '0 1px 4px rgba(0,0,0,0.06)',
        'amex-md':  '0 4px 16px rgba(0,111,207,0.10)',
        'amex-lg':  '0 8px 32px rgba(0,111,207,0.14)',
      },
      borderRadius: {
        amex: '4px',
      }
    }
  },
  plugins: []
}

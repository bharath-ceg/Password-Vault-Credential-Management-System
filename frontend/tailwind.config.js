/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        vault: {
          dark: '#F8FAFC',
          card: '#FFFFFF',
          surface: '#FFFFFF',
          border: '#E2E8F0',
          hover: '#F1F5F9',
          accent: '#2563EB',
          'accent-hover': '#1D4ED8',
          emerald: '#059669',
          amber: '#D97706',
          rose: '#DC2626',
          subtext: '#64748B',
          text: '#0F172A'
        }
      },
      fontFamily: {
        sans: ['Inter', 'Segoe UI', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}

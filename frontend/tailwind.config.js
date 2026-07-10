/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        accent: {
          DEFAULT: "#4F46E5", // indigo — the single accent per the UI/UX restraint spec
          hover: "#4338CA",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
      },
      spacing: {
        18: "4.5rem",
      },
      borderRadius: {
        DEFAULT: "8px",
        card: "12px",
        input: "6px",
        pill: "9999px",
      },
    },
  },
  plugins: [],
}

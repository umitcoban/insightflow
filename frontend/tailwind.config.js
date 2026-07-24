/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        graphite: {
          800: "#1f2937",
          900: "#111827"
        }
      }
    }
  },
  plugins: []
};

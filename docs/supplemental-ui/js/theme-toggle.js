// Redis OM Spring Documentation Theme Toggle
document.addEventListener('DOMContentLoaded', function() {
  // Debug log to verify script is running
  console.log("Theme toggle script loaded");
  
  const themeToggleButton = document.getElementById('theme-toggle-button');
  if (!themeToggleButton) {
    console.error("Theme toggle button not found in DOM");
    return;
  }
  
  console.log("Theme toggle button found:", themeToggleButton);
  
  // Function to set theme with debug
  function setTheme(isDark) {
    console.log("Setting theme:", isDark ? "dark" : "light");
    
    // First update the toggle UI for immediate feedback
    if (isDark) {
      themeToggleButton.classList.add('dark');
    } else {
      themeToggleButton.classList.remove('dark');
    }
    
    // Add animation class to thumb
    const toggleThumb = themeToggleButton.querySelector('.toggle-thumb');
    if (toggleThumb) {
      toggleThumb.classList.add('animating');
      setTimeout(() => {
        toggleThumb.classList.remove('animating');
      }, 300);
    }
    
    // Try to prevent flash by setting a CSS variable first
    if (isDark) {
      document.documentElement.style.setProperty('--page-background-color', 'var(--redis-dark-theme-bg)');
    } else {
      document.documentElement.style.setProperty('--page-background-color', 'white');
    }
    
    // Set the theme classes with no delay to prevent flash
    if (isDark) {
      document.documentElement.classList.add('dark-theme');
      document.body.classList.add('dark-theme');
      localStorage.setItem('redis-om-theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark-theme');
      document.body.classList.remove('dark-theme');
      localStorage.setItem('redis-om-theme', 'light');
    }
    
    console.log("Theme set:", isDark ? "dark" : "light", 
                "Body classes:", document.body.classList.toString(),
                "HTML classes:", document.documentElement.classList.toString(),
                "Button classes:", themeToggleButton.classList.toString());
  }
  
  // Initialize theme based on saved preference or system preference
  function initializeTheme() {
    const savedTheme = localStorage.getItem('redis-om-theme');
    console.log("Saved theme:", savedTheme);
    
    if (savedTheme === 'dark') {
      setTheme(true);
    } else if (savedTheme === 'light') {
      setTheme(false);
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      console.log("Using system dark mode preference");
      setTheme(true);
    } else {
      console.log("Defaulting to light theme");
      setTheme(false);
    }
  }
  
  // Initialize theme on page load
  initializeTheme();
  
  // Toggle theme on button click
  themeToggleButton.addEventListener('click', function() {
    console.log("Toggle button clicked");
    const isDark = document.body.classList.contains('dark-theme');
    setTheme(!isDark);
  });
  
  // Listen for system theme changes
  if (window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
      if (localStorage.getItem('redis-om-theme') === null) {
        console.log("System theme changed, applying:", e.matches ? "dark" : "light");
        setTheme(e.matches);
      }
    });
  }
});
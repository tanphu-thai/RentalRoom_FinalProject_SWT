const fs = require('fs');
const path = require('path');

const replacements = {
  'bg-card': 'bg-white dark:bg-slate-900',
  'text-card-foreground': 'text-slate-900 dark:text-slate-100',
  'border-border': 'border-slate-200 dark:border-slate-800',
  'bg-background': 'bg-slate-50 dark:bg-slate-950',
  'text-foreground': 'text-slate-900 dark:text-slate-100',
  'border-input': 'border-slate-300 dark:border-slate-700',
  'ring-ring': 'ring-blue-500',
  'bg-primary text-primary-foreground': 'bg-blue-600 text-white',
  'hover:bg-primary/90': 'hover:bg-blue-700',
  'text-primary': 'text-blue-600 dark:text-blue-400',
  'bg-secondary text-secondary-foreground': 'bg-slate-200 text-slate-900 dark:bg-slate-800 dark:text-slate-100',
  'hover:bg-secondary/80': 'hover:bg-slate-300 dark:hover:bg-slate-700',
  'bg-destructive text-destructive-foreground': 'bg-red-600 text-white',
  'hover:bg-destructive/90': 'hover:bg-red-700',
  'hover:bg-accent hover:text-accent-foreground': 'hover:bg-slate-200 dark:hover:bg-slate-800',
  'text-muted-foreground': 'text-slate-500 dark:text-slate-400',
  'divide-border': 'divide-slate-200 dark:divide-slate-800',
};

function walkDir(dir) {
  fs.readdirSync(dir).forEach(f => {
    const dirPath = path.join(dir, f);
    const isDirectory = fs.statSync(dirPath).isDirectory();
    if (isDirectory) {
      walkDir(dirPath);
    } else if (f.endsWith('.jsx') || f.endsWith('.js')) {
      let content = fs.readFileSync(dirPath, 'utf8');
      for (const [key, value] of Object.entries(replacements)) {
        content = content.replaceAll(key, value);
      }
      fs.writeFileSync(dirPath, content);
    }
  });
}

walkDir('./src');
console.log('Fixed Tailwind classes');

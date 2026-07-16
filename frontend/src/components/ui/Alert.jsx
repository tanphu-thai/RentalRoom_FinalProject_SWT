import React from 'react';
import { XCircle, CheckCircle, X } from 'lucide-react';

export function Alert({ notice, onClose }) {
  if (!notice) return null;
  const isError = notice.type === 'error';

  return (
    <div className={`flex items-center justify-between p-4 mb-4 rounded-lg shadow-sm border ${
      isError 
        ? 'bg-destructive/10 text-destructive border-destructive/20' 
        : 'bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-400 dark:border-green-800'
    }`}>
      <div className="flex items-center gap-3">
        {isError ? <XCircle className="w-5 h-5" /> : <CheckCircle className="w-5 h-5" />}
        <span className="font-medium text-sm">{notice.message}</span>
      </div>
      <button onClick={onClose} className="hover:opacity-70 transition-opacity">
        <X className="w-5 h-5" />
      </button>
    </div>
  );
}

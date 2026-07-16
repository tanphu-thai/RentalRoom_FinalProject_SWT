import React, { useState, useEffect } from 'react';
import { Server, WifiOff } from 'lucide-react';

export function BackendStatus() {
  const [isUp, setIsUp] = useState(false);

  useEffect(() => {
    let mounted = true;
    const checkStatus = async () => {
      try {
        const res = await fetch('/api/rooms', { method: 'HEAD' });
        // Vite proxy returns 502/504 when backend is down
        if (mounted) {
          if (res.status === 502 || res.status === 504 || res.status === 404) {
            setIsUp(false);
          } else {
            setIsUp(true);
          }
        }
      } catch (err) {
        if (mounted) setIsUp(false);
      }
    };
    
    checkStatus();
    const interval = setInterval(checkStatus, 3000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  return (
    <div className={`fixed top-6 right-48 z-[9999] px-3 py-1.5 rounded-full shadow-lg border text-xs font-semibold flex items-center gap-1.5 transition-colors ${
      isUp 
        ? 'bg-emerald-100 text-emerald-700 border-emerald-300 dark:bg-emerald-950 dark:text-emerald-400 dark:border-emerald-800' 
        : 'bg-rose-100 text-rose-700 border-rose-300 dark:bg-rose-950 dark:text-rose-400 dark:border-rose-800 animate-pulse'
    }`}>
      {isUp ? <Server size={14} /> : <WifiOff size={14} />}
      {isUp ? 'Backend Connected' : 'Backend Disconnected'}
    </div>
  );
}

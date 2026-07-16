import React from 'react';
import { Outlet } from 'react-router-dom';
import { LogOut } from 'lucide-react';
import { ThemeToggle } from '../components/ui/ThemeToggle.jsx';

export function TenantLayout({ user, onLogout }) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 dark:from-indigo-950 dark:via-purple-950 dark:to-pink-950 transition-colors duration-500">
      {/* Navbar */}
      <header className="glass sticky top-0 z-50 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center font-black text-white text-xl shadow-inner">
            RR
          </div>
          <div>
            <h1 className="text-lg font-bold text-white leading-tight">Tenant Portal</h1>
            <p className="text-xs text-white/80 font-medium">Welcome, {user?.username}</p>
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="bg-white/20 rounded-full flex items-center justify-center">
            <ThemeToggle />
          </div>
          <button 
            onClick={onLogout}
            className="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-4 py-2 rounded-full font-medium transition-colors text-sm shadow-sm backdrop-blur-md border border-white/20"
          >
            <LogOut className="w-4 h-4" />
            Logout
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto p-6 md:p-8">
        <Outlet />
      </main>
    </div>
  );
}

import { ShoppingCart, Package, Home } from 'lucide-react';
import logo from './assets/img/Logo.png';


function App() {
  return (
    <div className="min-h-screen bg-slate-900 text-white font-sans">
      {/* Navbar de ejemplo */}
      <nav className="border-b border-slate-800 bg-slate-900/50 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-2">
              <img src={logo} alt="L'empanada Logo" className="w-10 h-10 object-contain rounded-full shadow-lg shadow-red-50/20" />

              <span className="text-xl font-semibold tracking-tight text-white/90 antialiased">
                L'empanada
              </span>
            </div>

            <div className="flex items-center gap-6">
              <a href="#" className="text-slate-300 hover:text-white transition-colors flex items-center gap-1">
                <Home size={18} />
                <span>Tienda</span>
              </a>
              <button className="relative p-2 text-slate-300 hover:text-white transition-all hover:scale-110">
                <ShoppingCart size={24} />
                <span className="absolute -top-1 -right-1 bg-indigo-600 text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center border-2 border-slate-900 shadow-lg">
                  0
                </span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center mb-16">
          <h1 className="text-5xl font-extrabold mb-4 bg-gradient-to-b from-white to-slate-500 bg-clip-text text-transparent">
            Bienvenue a l'empanada!
          </h1>
          <p className="text-slate-400 text-lg max-w-2xl mx-auto">
            Explorez une expérience d'achat colombienne authentique!
          </p>
        </div>

        {/* Tarjeta de Prueba */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[1, 2, 3].map((i) => (
            <div key={i} className="group bg-slate-800/50 border border-slate-700 rounded-3xl p-6 hover:bg-slate-800 transition-all duration-300 hover:shadow-2xl hover:shadow-indigo-500/10 hover:-translate-y-1">
              <div className="aspect-square bg-slate-700 rounded-2xl mb-6 overflow-hidden flex items-center justify-center text-slate-500">
                <Package size={64} className="group-hover:scale-110 transition-transform duration-500" />
              </div>
              <h3 className="text-xl font-bold mb-2">Producto de Ejemplo #{i}</h3>
              <p className="text-slate-400 text-sm mb-6">
                Este es un producto de prueba para verificar que el diseño con Tailwind y TypeScript funciona correctamente.
              </p>
              <div className="flex items-center justify-between">
                <span className="text-2xl font-bold text-indigo-400">$29.99</span>
                <button className="bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-2 rounded-xl font-semibold transition-all shadow-lg shadow-indigo-600/20 active:scale-95">
                  Añadir
                </button>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

export default App;

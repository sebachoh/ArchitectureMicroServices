import { ShoppingCart, Home, Star, UtensilsCrossed } from 'lucide-react';
import logo from '../assets/img/Logo.png';
import empanadaImg from '../assets/img/products/1-empanada.png';
import arepaImg from '../assets/img/products/2-arepa.jpg';
import bandejaImg from '../assets/img/products/3-bandeja.jpg';
import { useCart } from '../context/CartContext';

const PRODUCTS = [
    {
        id: 1,
        name: "Empanada",
        description: "Pâte de maïs croustillante farcie de viande effilochée et de pommes de terre, accompagnée d’ají maison.",
        price: 3.00,
        image: empanadaImg,
        rating: 4.9
    },
    {
        id: 2,
        name: "Arepa",
        description: "Arepa de maïs avec une généreuse couche de fromage paysan fondu.",
        price: 4.90,
        image: arepaImg,
        rating: 4.8
    },
    {
        id: 3,
        name: "Bandeja Paisa",
        description: "Le plat emblématique : haricots, riz, chicharrón, œuf, viande, avocat et bien plus encore.",
        price: 12.45,
        image: bandejaImg,
        rating: 5.0
    }
];

export function StoreContent() {
    const { addToCart, toggleCart, totalItems } = useCart();

    return (
        <div className="min-h-screen bg-[#0c0a09] text-stone-100 font-sans selection:bg-orange-500/30">

            {/* Navbar Premium */}
            <nav className="border-b border-white/5 bg-stone-950/80 backdrop-blur-xl sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-20">
                        <div className="flex items-center gap-3 group cursor-pointer">
                            <div className="relative">
                                <div className="absolute inset-0 bg-orange-500 blur-lg opacity-20 group-hover:opacity-40 transition-opacity rounded-full"></div>
                                <img
                                    src={logo}
                                    alt="L'Empanada Logo"
                                    className="w-12 h-12 object-contain rounded-full relative z-10 border border-white/10"
                                />
                            </div>

                            <span className="text-2xl font-semibold tracking-tight text-white antialiased">
                                L'Empanada
                            </span>
                        </div>

                        <div className="flex items-center gap-8">
                            <a href="#" className="text-stone-400 hover:text-orange-400 transition-colors flex items-center gap-2 text-sm font-medium">
                                <Home size={18} />
                                <span>Magasin</span>
                            </a>
                            <button
                                onClick={toggleCart}
                                className="relative p-3 text-stone-400 hover:text-white transition-all hover:bg-white/5 rounded-full group"
                            >
                                <ShoppingCart size={22} />
                                {totalItems > 0 && (
                                    <span className="absolute top-1 right-1 bg-orange-600 text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center text-white ring-2 ring-stone-950 group-hover:scale-110 transition-transform animate-in zoom-in">
                                        {totalItems}
                                    </span>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Hero Section Aesthetic */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                <div className="text-center mb-24 relative">
                    <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-orange-500/10 border border-orange-500/20 text-orange-400 text-xs font-semibold uppercase tracking-wider mb-6">
                        <UtensilsCrossed size={12} />
                        Saveurs Authentiques
                    </div>
                    <h1 className="text-6xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-orange-100 via-orange-50 to-stone-400 bg-clip-text text-transparent tracking-tight">
                        Bienvenue à l'Empanada !
                    </h1>
                    <p className="text-stone-400 text-xl max-w-2xl mx-auto leading-relaxed">
                        Explorez une expérience d'achat colombienne authentique, préparée avec passion et tradition.
                    </p>
                </div>

                {/* Grid de Productos Rediseñado */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {PRODUCTS.map((product) => (
                        <div key={product.id} className="group relative bg-stone-900 border border-white/5 rounded-[2rem] p-4 hover:border-orange-500/30 transition-all duration-500 hover:-translate-y-2">
                            {/* Imagen del Producto */}
                            <div className="aspect-[4/3] rounded-[1.5rem] mb-6 overflow-hidden relative">
                                <div className="absolute inset-0 bg-gradient-to-t from-stone-950/80 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-10" />
                                <img
                                    src={product.image}
                                    alt={product.name}
                                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-in-out"
                                />

                                {/* Rating Badge */}
                                <div className="absolute top-4 right-4 bg-black/40 backdrop-blur-md border border-white/10 px-2 py-1 rounded-lg flex items-center gap-1 text-xs font-bold text-white z-20">
                                    <Star size={10} className="text-yellow-400 fill-yellow-400" />
                                    {product.rating}
                                </div>
                            </div>

                            {/* Contenido de la Tarjeta */}
                            <div className="px-2 pb-2">
                                <h3 className="text-2xl font-bold mb-2 text-stone-100 group-hover:text-orange-400 transition-colors">{product.name}</h3>
                                <p className="text-stone-500 text-sm mb-6 leading-relaxed line-clamp-2">
                                    {product.description}
                                </p>

                                <div className="flex items-center justify-between mt-auto">
                                    <div className="flex flex-col">
                                        <span className="text-xs text-stone-500 font-medium uppercase tracking-wider">Prix</span>
                                        <span className="text-2xl font-bold text-white">{product.price.toFixed(2)}€</span>
                                    </div>
                                    <button
                                        onClick={() => addToCart(product)}
                                        className="bg-white text-stone-950 px-6 py-3 rounded-2xl font-bold text-sm hover:bg-orange-500 hover:text-white transition-all duration-300 shadow-xl shadow-black/20 flex items-center gap-2 group/btn active:scale-95"
                                    >
                                        Ajouter
                                        <ShoppingCart size={16} className="group-hover/btn:translate-x-1 transition-transform" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
}

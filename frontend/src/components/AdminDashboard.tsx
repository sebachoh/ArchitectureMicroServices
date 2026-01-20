import { useState, useEffect } from 'react';
import { Terminal, Database, CreditCard, ShoppingCart, Truck, RefreshCw, AlertCircle, CheckCircle2 } from 'lucide-react';
import api from '../services/api';
import { useCart } from '../context/CartContext';

type ServiceTab = 'catalogue' | 'panier' | 'paiement' | 'tracking';

interface ServiceConfig {
    id: ServiceTab;
    name: string;
    port: string;
    endpoint: string;
    icon: any;
    description: string;
}

const SERVICES: ServiceConfig[] = [
    {
        id: 'catalogue',
        name: 'Catalogue Service',
        port: '8081',
        endpoint: '/api/products',
        icon: Database,
        description: 'Gestion des produits et stocks'
    },
    {
        id: 'panier',
        name: 'Panier Service',
        port: '8082',
        endpoint: '/cart', // Sujeto a verificación de endpoint real
        icon: ShoppingCart,
        description: 'Gestion du panier temporaire'
    },
    {
        id: 'paiement',
        name: 'Paiement Service',
        port: '8083',
        endpoint: '/api/payments', // Sujeto a verificación
        icon: CreditCard,
        description: 'Traitement des transactions'
    },
    {
        id: 'tracking',
        name: 'Tracking Service/Suivi',
        port: '8084',
        endpoint: '/api/tracking', // Sujeto a verificación
        icon: Truck,
        description: 'Suivi des commandes'
    }
];

export function AdminDashboard() {
    const { toggleRole } = useCart();
    const [activeTab, setActiveTab] = useState<ServiceTab>('catalogue');
    const [data, setData] = useState<any>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<number | null>(null);

    const fetchData = async () => {
        setLoading(true);
        setError(null);
        setData(null);
        setStatus(null);

        const service = SERVICES.find(s => s.id === activeTab)!;

        try {
            // Nota: En un entorno real dockerizado, el navegador no accede directo a los puertos del contenedor
            // Por lo que usamos el Gateway (8080) que ya está configurado en `api`.
            // Si el usuario corre todo en localhost sin docker network aislado, podría funcionar directo,
            // pero para consistencia usamos la ruta relativa del proxy/gateway.

            // Ajuste de endpoints según lo que sea más probable que responda algo interesante
            let url = service.endpoint;

            // Para Panier, intentamos obtener el carrito actual (aunque puede estar vacío)
            if (activeTab === 'panier') {
                // Si el backend requiere ID, esto podría fallar, pero intentaremos el endpoint genérico
            }

            const response = await api.get(url);
            setData(response.data);
            setStatus(response.status);
        } catch (err: any) {
            console.error(`Error fetching ${service.name}:`, err);
            setError(err.message || 'Erreur de connexion au service');
            if (err.response) {
                setStatus(err.response.status);
                setData(err.response.data); // A veces el error trae JSON útil
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [activeTab]);

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-green-500 font-mono flex flex-col">
            {/* Header 'Hacker Style' */}
            <header className="border-b border-green-900/30 bg-black p-4 flex justify-between items-center sticky top-0 z-50">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-green-900/20 rounded-md border border-green-500/30">
                        <Terminal size={24} />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold tracking-wider text-green-400">ADMIN_CONSOLE</h1>
                        <p className="text-xs text-green-700">PROJET DE MICRO-SERVICES</p>
                    </div>
                </div>

                <div className="flex items-center gap-6">
                    <div className="flex items-center gap-2 text-xs text-green-600">
                        <span className="relative flex h-2 w-2">
                            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                            <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                        </span>
                        GATEWAY: EN LIGNE (8080)
                    </div>
                    <button
                        onClick={toggleRole}
                        className="bg-green-900/20 hover:bg-green-800/30 text-green-400 border border-green-700/50 px-4 py-2 rounded transition-colors text-sm font-bold tracking-widest uppercase hover:shadow-[0_0_10px_rgba(74,222,128,0.2)]"
                    >
                        Sortir du mode Admin
                    </button>
                </div>
            </header>

            <div className="flex flex-1 overflow-hidden">
                {/* Sidebar Menu */}
                <aside className="w-64 bg-black border-r border-green-900/30 flex flex-col">
                    <div className="p-4 text-xs font-bold text-green-800 uppercase tracking-widest mb-2">Microservices</div>
                    <nav className="flex-1 space-y-1 px-2">
                        {SERVICES.map((service) => {
                            const Icon = service.icon;
                            const isActive = activeTab === service.id;
                            return (
                                <button
                                    key={service.id}
                                    onClick={() => setActiveTab(service.id)}
                                    className={`w-full text-left px-4 py-3 rounded-lg flex items-center gap-3 transition-all ${isActive
                                        ? 'bg-green-900/20 text-green-300 border border-green-500/30 shadow-[0_0_15px_rgba(74,222,128,0.1)]'
                                        : 'text-green-700 hover:text-green-500 hover:bg-green-900/10'
                                        }`}
                                >
                                    <Icon size={18} />
                                    <div>
                                        <div className="font-bold">{service.name}</div>
                                        <div className="text-[10px] opacity-70">Port: {service.port}</div>
                                    </div>
                                    {isActive && <div className="ml-auto w-1.5 h-1.5 rounded-full bg-green-500 shadow-[0_0_5px_#22c55e]" />}
                                </button>
                            );
                        })}
                    </nav>
                    <div className="p-4 border-t border-green-900/30 text-[10px] text-green-800 text-center">
                        JAVIER PARRA - SEBASTIAN RUIZ - JOSE VILLA
                    </div>
                </aside>

                {/* Main Content Area */}
                <main className="flex-1 flex flex-col h-full overflow-hidden bg-[#0c0c0c] relative">
                    {/* Background Scanlines */}
                    <div className="absolute inset-0 pointer-events-none bg-[linear-gradient(rgba(18,16,16,0)_50%,rgba(0,0,0,0.25)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(0,255,0,0.02),rgba(0,0,255,0.06))] z-0 bg-[length:100%_4px,3px_100%] opacity-20" />

                    {/* Toolbar */}
                    <div className="p-4 border-b border-green-900/30 flex justify-between items-center z-10 bg-black/50 backdrop-blur-sm">
                        <div className="flex items-center gap-4">
                            <h2 className="text-lg font-bold text-green-400 flex items-center gap-2">
                                Endpoint: <code className="bg-green-900/30 px-2 py-1 rounded text-sm">{SERVICES.find(s => s.id === activeTab)?.endpoint}</code>
                            </h2>
                            {status && (
                                <span className={`text-xs px-2 py-1 rounded font-bold ${status >= 200 && status < 300 ? 'bg-green-900/40 text-green-400' : 'bg-red-900/40 text-red-400'}`}>
                                    STATUS: {status}
                                </span>
                            )}
                        </div>
                        <button
                            onClick={fetchData}
                            disabled={loading}
                            className="p-2 hover:bg-green-900/20 rounded-full text-green-500 transition-colors disabled:opacity-50"
                        >
                            <RefreshCw size={20} className={loading ? 'animate-spin' : ''} />
                        </button>
                    </div>

                    {/* JSON Display */}
                    <div className="flex-1 overflow-auto p-6 z-10">
                        {loading ? (
                            <div className="h-full flex flex-col items-center justify-center gap-4 text-green-600/50">
                                <RefreshCw className="animate-spin w-12 h-12" />
                                <p className="animate-pulse">CONNEXION...</p>
                            </div>
                        ) : error ? (
                            <div className="bg-red-900/10 border border-red-500/30 p-6 rounded-lg max-w-2xl mx-auto mt-10">
                                <div className="flex items-center gap-3 text-red-500 mb-4">
                                    <AlertCircle size={24} />
                                    <h3 className="text-xl font-bold">CONNEXION IMPOSSIBLE</h3>
                                </div>
                                <p className="text-red-400 mb-4">{error}</p>
                                <pre className="text-xs bg-black/50 p-4 rounded text-red-300/70 overflow-x-auto">
                                    {JSON.stringify(data, null, 2)}
                                </pre>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                <div className="flex items-center gap-2 text-xs text-green-700 mb-2">
                                    <CheckCircle2 size={14} />
                                    <span>DONNÉES REÇUES AVEC SUCCES</span>
                                    <span className="ml-auto opacity-50">{new Date().toLocaleTimeString()}</span>
                                </div>
                                <div className="relative group">
                                    <div className="absolute inset-0 bg-green-500/5 blur-xl group-hover:bg-green-500/10 transition-all duration-500" />
                                    <pre className="relative bg-black/80 border border-green-500/20 p-6 rounded-xl overflow-x-auto text-sm text-green-300 shadow-2xl">
                                        <code>{JSON.stringify(data, null, 2)}</code>
                                    </pre>
                                </div>
                            </div>
                        )}
                    </div>
                </main>
            </div>
        </div>
    );
}

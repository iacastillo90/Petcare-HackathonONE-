/**
 * PetCare Code Transcriber - by ArcPet
 * * Este script escanea todos los archivos sueltos en la raíz y todo el contenido
 * de la carpeta 'src', transcribiéndolos a un único archivo de texto con
 * separadores visuales, rutas y detección de packages de Java.
 */

const fs = require('fs');
const path = require('path');

const OUTPUT_FILE = 'codigo_completo_petcare.txt';
const ROOT_DIR = __dirname;
const SRC_DIR = path.join(ROOT_DIR, 'src');

// Extensiones de archivos binarios o irrelevantes que no queremos transcribir
const EXCLUDE_EXT = ['.jar', '.class', '.png', '.jpg', '.jpeg', '.ico', '.pdf', '.zip', '.exe', '.git'];

// 1. Limpiar el archivo de salida si ya existía de una ejecución anterior
if (fs.existsSync(OUTPUT_FILE)) {
    fs.unlinkSync(OUTPUT_FILE);
}

/**
 * Lee un archivo, genera su cabecera visual y lo añade al documento final.
 */
function appendToFile(filePath, relativePath) {
    const ext = path.extname(filePath).toLowerCase();

    // Ignorar archivos no legibles como imágenes o binarios
    if (EXCLUDE_EXT.includes(ext)) return;

    try {
        const stats = fs.statSync(filePath);
        if (!stats.isFile()) return;

        const content = fs.readFileSync(filePath, 'utf8');

        // --- DISEÑO DEL SEPARADOR VISUAL ---
        const border = '='.repeat(85);
        let header = `\n${border}\n`;
        header += `📂 RUTA:    ${relativePath}\n`;
        header += `📄 ARCHIVO: ${path.basename(filePath)}\n`;

        // Inteligencia extra: Si es un archivo Java, extraemos su Package
        if (ext === '.java') {
            const packageMatch = content.match(/^package\s+([^;]+);/m);
            if (packageMatch) {
                header += `📦 PACKAGE: ${packageMatch[1]}\n`;
            }
        }

        header += `${border}\n\n`;

        // Escribir en el archivo final
        fs.appendFileSync(OUTPUT_FILE, header + content + '\n');
        console.log(`✔️ Transcrito: ${relativePath}`);

    } catch (err) {
        console.error(`❌ Error al leer ${relativePath}: ${err.message}`);
    }
}

/**
 * Escanea recursivamente una carpeta y sus subcarpetas.
 */
function scanDirectory(dir) {
    if (!fs.existsSync(dir)) return;

    const items = fs.readdirSync(dir);
    for (const item of items) {
        const fullPath = path.join(dir, item);
        const relativePath = path.relative(ROOT_DIR, fullPath);

        if (fs.statSync(fullPath).isDirectory()) {
            scanDirectory(fullPath); // Llamada recursiva para entrar a subcarpetas
        } else {
            appendToFile(fullPath, relativePath);
        }
    }
}

console.log('🚀 Iniciando Arquitecto Code Scanner de PetCare...\n');

// PASO 1: Procesar estrictamente SOLO los archivos sueltos en la raíz
const rootItems = fs.readdirSync(ROOT_DIR);
for (const item of rootItems) {
    const fullPath = path.join(ROOT_DIR, item);
    const relativePath = path.relative(ROOT_DIR, fullPath);

    // Evitar transcribir este mismo script y el archivo de salida
    if (item === path.basename(__filename) || item === OUTPUT_FILE) continue;

    if (fs.statSync(fullPath).isFile()) {
        appendToFile(fullPath, relativePath);
    }
}

// PASO 2: Procesar toda la carpeta 'src' recursivamente
console.log('\n🔍 Escaneando carpeta src y sus packages...');
scanDirectory(SRC_DIR);

console.log(`\n✅ ¡Proceso finalizado con éxito!`);
console.log(`📁 Todo el código ha sido unificado en: ${path.join(ROOT_DIR, OUTPUT_FILE)}`);
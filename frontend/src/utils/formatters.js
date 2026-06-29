export const obtenerIniciales = (nombre, apellidos) => {
  const n = nombre ? nombre.charAt(0).toUpperCase() : 'U'
  const a = apellidos ? apellidos.trim().split(/\s+/)[0].charAt(0).toUpperCase() : ''
  return (n + a) || 'U'
}

export const formatearDinero = (cantidad) => {
  if (cantidad === null || cantidad === undefined || isNaN(cantidad)) return '0.00€'
  return Number(cantidad).toFixed(2) + '€'
}

export const formatearFecha = (fecha, opciones = { day: '2-digit', month: 'long', year: 'numeric' }) => {
  if (!fecha) return ''
  const date = new Date(fecha)
  return new Intl.DateTimeFormat('es-ES', opciones).format(date)
}

export const formatearHora = (hora) => {
  if (!hora) return ''
  return hora.substring(0, 5)
}

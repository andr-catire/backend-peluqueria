export function notFound(_request, response) {
  response.status(404).json({ error: 'NOT_FOUND', message: 'Ruta no encontrada' });
}

export function errorHandler(error, _request, response, _next) {
  console.error(error);
  response.status(error.status || 500).json({
    error: error.code || 'INTERNAL_ERROR',
    message: error.status ? error.message : 'Error interno del servidor',
  });
}

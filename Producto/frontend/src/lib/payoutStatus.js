// Deriva "PAGADO"/"PENDIENTE" por campaña a partir del total ya transferido a la ONG.
// Como los pagos se registran por ONG (no por campaña), es una ESTIMACIÓN: el total
// transferido va "cubriendo" las campañas desde la ÚLTIMA hacia la primera; en cuanto
// una no se cubre por completo, esa y las anteriores quedan PENDIENTE (así las PENDIENTE
// suman el saldo aún adeudado). Devuelve las campañas con un campo `status` agregado.
export function withPaymentStatus(campaigns, totalPaidOut) {
  const list = campaigns ?? [];
  let remaining = Number(totalPaidOut) || 0;
  let covering = true;
  const statuses = new Array(list.length);
  for (let i = list.length - 1; i >= 0; i--) {
    const amt = Number(list[i].amount) || 0;
    if (covering && remaining + 0.001 >= amt) {
      remaining -= amt;
      statuses[i] = "PAGADO";
    } else {
      covering = false;
      statuses[i] = "PENDIENTE";
    }
  }
  return list.map((c, i) => ({ ...c, status: statuses[i] }));
}

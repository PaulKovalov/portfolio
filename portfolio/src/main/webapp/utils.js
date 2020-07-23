function humanReadableDateFromTimestamp(timestamp) {
  const dateObj = new Date(Number(timestamp));
  // 10 first characters (yyyy-mm-dd) are human readable, other is not
  return dateObj.toISOString().slice(0, 10) + ' at ' +
         (dateObj.getHours() < 10 ? '0' + dateObj.getHours() : dateObj.getHours()) + ':' +
         (dateObj.getMinutes() < 10 ? '0' + dateObj.getMinutes() : dateObj.getMinutes()) + ':' +
         (dateObj.getSeconds() < 10 ? '0' + dateObj.getSeconds() : dateObj.getSeconds());
}

function total(p, q, c, vip, ciudad) {
  var x = p * q;
  if (c == "PROMO30") {
    x = x - x * 0.30;
  } else {
    if (vip == true) {
      x = x - x * 0.10;
    }
  }
  if (ciudad == "Bogota") {
    x = x + 8000;
  } else {
    x = x + 15000;
  }
  if (q > 5) {
    x = x - x * 0.10;
  }
  if (vip == true) {
    x = x - x * 0.10;
  }
  console.log("TOTAL=" + x);
  return x;
}

function pagar() {
  try {
    var p = document.querySelector("#precio").value;
    var q = document.querySelector("#cantidad").value;
    alert(total(p,q,"promo30",false,"Bogotá"));
  } catch(e) {
    alert("ERROR 9");
  }
}

"""
Mock KabhawiMS gateway used to record the demo video of the admin app.

It mimics the real endpoints (AuthForge / Product-server / Order-server via
GateWay-server) with Arabic sample data, including the real server quirks
(no categoryId in getAllProducts, plain-text JWT from /login, ...).

Usage:  python3 mock_server.py [--port 8080]
Requires Pillow for the product images (pip install pillow).
"""
import argparse
import base64
import json
import os
import re
import tempfile
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, unquote, urlparse

import demo_images

ADMIN = {
    "username": "admin@kabhawi.com",
    "firstName": "محمد",
    "lastName": "السالم",
    "phoneNumber": "0521234567",
    "uuid": "a1b2c3d4-0000-4000-8000-000000000001",
    "role": "ADMIN",
}

CATEGORIES = [
    {"id": 1, "name": "خواتم", "image": "cat_rings"},
    {"id": 2, "name": "قلائد وعقود", "image": "cat_necklaces"},
    {"id": 3, "name": "ساعات", "image": "cat_watches"},
    {"id": 4, "name": "أساور", "image": "cat_bracelets"},
    {"id": 5, "name": "أقراط", "image": "cat_earrings"},
    {"id": 6, "name": "أطقم العرائس", "image": "cat_sets"},
]

# (name, barcode, price, qty, categoryId, weight, materials, image, description)
PRODUCTS = [
    ("خاتم ذهب عيار 21 مرصع", "7290010001", 1450.0, 6, 1, 4.2, ["ذهب عيار 21", "زركون"], "ring_gold",
     "خاتم نسائي من الذهب عيار 21 مرصع بفصوص زركون لامعة، مناسب للخطوبة والمناسبات."),
    ("خاتم فضة بحجر الفيروز", "7290010002", 180.0, 3, 1, 6.5, ["فضة 925", "فيروز"], "ring_turquoise",
     "خاتم فضة عيار 925 بحجر فيروز طبيعي."),
    ("عقد لؤلؤ طبيعي", "7290010003", 890.0, 0, 2, 18.0, ["لؤلؤ طبيعي", "ذهب أبيض"], "pearl_necklace",
     "عقد من اللؤلؤ الطبيعي مع قفل من الذهب الأبيض."),
    ("قلادة ذهب بتصميم الهلال", "7290010004", 1200.0, 9, 2, 3.8, ["ذهب عيار 18", "ياقوت"], "crescent_pendant",
     "قلادة ناعمة على شكل هلال مع حجر ياقوت صغير."),
    ("ساعة كلاسيكية بحزام جلد", "7290010005", 650.0, 12, 3, 45.0, ["جلد طبيعي", "ستانلس ستيل"], "watch_leather",
     "ساعة رجالية كلاسيكية مقاومة للماء بحزام جلد بني."),
    ("ساعة نسائية ذهبية", "7290010006", 980.0, 2, 3, 38.0, ["ستانلس ستيل مطلي ذهب", "كريستال"], "watch_gold",
     "ساعة نسائية أنيقة مطلية بالذهب مع مؤشرات كريستالية."),
    ("سوار ذهب مجدول", "7290010007", 2100.0, 4, 4, 9.6, ["ذهب عيار 21"], "bracelet_gold",
     "سوار ذهب مجدول بثلاث طبقات."),
    ("سوار فضة للأطفال", "7290010008", 150.0, 25, 4, 5.0, ["فضة 925", "مينا ملونة"], "bracelet_silver",
     "سوار فضة خفيف للأطفال مع تعليقات ملونة."),
    ("أقراط ذهب على شكل وردة", "7290010009", 540.0, 7, 5, 2.9, ["ذهب عيار 18", "زركون"], "earrings_flower",
     "أقراط متدلية على شكل وردة من الذهب عيار 18."),
    ("طقم عروس ذهب وزمرد", "7290010010", 5200.0, 1, 6, 42.0, ["ذهب عيار 21", "زمرد", "زركون"], "bridal_set",
     "طقم كامل للعروس: قلادة وخاتم وقرط مرصع بالزمرد."),
]

CUSTOMERS = [
    ("sara.k@gmail.com", "سارة", "خطيب", "0547788123", "b0000000-0000-4000-8000-000000000011"),
    ("ahmad.m@hotmail.com", "أحمد", "محاميد", "0509876543", "b0000000-0000-4000-8000-000000000012"),
    ("rana.z@gmail.com", "رنا", "زعبي", "0523344556", "b0000000-0000-4000-8000-000000000013"),
    ("yousef.a@gmail.com", "يوسف", "عثامنة", "0536677889", "b0000000-0000-4000-8000-000000000014"),
    ("lina.h@yahoo.com", "لينا", "حاج يحيى", "0584455667", "b0000000-0000-4000-8000-000000000015"),
    ("omar.s@gmail.com", "عمر", "صالح", "0501122334", "b0000000-0000-4000-8000-000000000016"),
]

# (id, customer index or None for guest, guest name, city, status, [(barcode, qty)])
ORDERS = [
    (1001, 0, None, "الناصرة، شارع بولس السادس 12", "SHIPPED", [("7290010001", 1)]),
    (1002, 1, None, "حيفا، شارع الجبل 45", "SHIPPED", [("7290010005", 1), ("7290010008", 2)]),
    (1003, None, ("هبة", "مصاروة", "0528899001", "heba.m@gmail.com"), "الطيبة، الحي الغربي", "CANCELLED",
     [("7290010003", 1)]),
    (1004, 2, None, "أم الفحم، عين إبراهيم", "SHIPPED", [("7290010009", 2)]),
    (1005, 3, None, "رهط، حي 7", "SHIPPED", [("7290010007", 1)]),
    (1006, 0, None, "الناصرة، شارع بولس السادس 12", "SHIPPED", [("7290010004", 1), ("7290010002", 1)]),
    (1007, 4, None, "باقة الغربية، الشارع الرئيسي", "CREATED", [("7290010006", 1)]),
    (1008, None, ("خالد", "جبارين", "0549090909", "khaled.j@gmail.com"), "كفر قاسم، شارع المسجد", "CREATED",
     [("7290010008", 3), ("7290010002", 1)]),
    (1009, 5, None, "شفاعمرو، حي الفوار", "CREATED", [("7290010010", 1)]),
    (1010, 1, None, "حيفا، شارع الجبل 45", "CREATED", [("7290010001", 1), ("7290010009", 1)]),
    (1011, 2, None, "أم الفحم، عين إبراهيم", "CREATED", [("7290010004", 2)]),
    (1012, None, ("نور", "أبو ريا", "0521212121", "noor.ar@gmail.com"), "سخنين، الحي الشرقي", "CREATED",
     [("7290010005", 1)]),
]

IMAGE_DIR = os.path.join(tempfile.gettempdir(), "kabhawi_demo_images")


class Store:
    def __init__(self):
        self.products = [
            {"name": n, "barcode": b, "price": p, "quantity": q, "categoryId": c, "weight": w,
             "materials": m, "images": [img], "description": desc}
            for (n, b, p, q, c, w, m, img, desc) in PRODUCTS
        ]
        self.users = [ADMIN, {
            "username": "manager@kabhawi.com", "firstName": "ليلى", "lastName": "السالم",
            "phoneNumber": "0527654321", "uuid": "a1b2c3d4-0000-4000-8000-000000000002", "role": "ADMIN"}]
        self.users += [{"username": u, "firstName": f, "lastName": l, "phoneNumber": ph, "uuid": uid, "role": "USER"}
                       for (u, f, l, ph, uid) in CUSTOMERS]
        price = {p["barcode"]: p["price"] for p in self.products}
        self.orders = []
        for oid, cust, guest, address, status, items in ORDERS:
            if cust is not None:
                u, f, l, ph, uid = CUSTOMERS[cust]
                who = {"userId": uid, "firstName": f, "lastName": l, "phoneNumber": ph, "email": u}
            else:
                f, l, ph, email = guest
                who = {"userId": None, "firstName": f, "lastName": l, "phoneNumber": ph, "email": email}
            lines = [{"barcode": b, "quantity": q, "price": price[b], "totalPrice": price[b] * q} for b, q in items]
            self.orders.append({"id": oid, "cartId": f"cart-{oid}", "address": address, "status": status,
                                "totalAmount": sum(x["totalPrice"] for x in lines), "items": lines, **who})

    def product_json(self, p, base, with_category):
        return {
            "name": p["name"], "description": p["description"], "price": p["price"], "barcode": p["barcode"],
            "quantity": p["quantity"],
            # مثل الخادم الحقيقي: getAllProducts لا يعيد categoryId
            "categoryId": p["categoryId"] if with_category else None,
            "weight": p["weight"], "materials": p["materials"],
            "imageUrl": [f"{base}/images/{i}.jpg" if not i.startswith("http") else i for i in p["images"]],
        }

    def categories_json(self, base):
        return [{
            "id": c["id"], "name": c["name"], "imageUrl": f"{base}/images/{c['image']}.jpg",
            "products": [self.product_json(p, base, False) for p in self.products if p["categoryId"] == c["id"]],
        } for c in CATEGORIES]


STORE = Store()


def make_token():
    def b64(obj):
        raw = json.dumps(obj, separators=(",", ":")).encode()
        return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()
    payload = {**{k: ADMIN[k] for k in ("username", "firstName", "lastName", "phoneNumber", "uuid", "role")},
               "sub": ADMIN["username"], "iat": int(time.time()), "exp": int(time.time()) + 24 * 3600}
    return f"{b64({'alg': 'HS256'})}.{b64(payload)}.ZGVtby1zaWduYXR1cmU"


def parse_multipart_json(body, content_type, part_name):
    m = re.search(r'boundary="?([^";]+)"?', content_type or "")
    if not m:
        return None
    for part in body.split(b"--" + m.group(1).encode()):
        if f'name="{part_name}"'.encode() in part:
            payload = part.split(b"\r\n\r\n", 1)[1].rsplit(b"\r\n", 1)[0]
            return json.loads(payload.decode())
    return None


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def base(self):
        return f"http://{self.headers.get('Host', 'localhost:8080')}"

    def send(self, status, body=None, content_type="application/json"):
        data = b""
        if body is not None:
            data = body if isinstance(body, bytes) else (
                body.encode() if isinstance(body, str) else json.dumps(body, ensure_ascii=False).encode())
        self.send_response(status)
        self.send_header("Content-Type", content_type + ("; charset=utf-8" if "json" in content_type or "text" in content_type else ""))
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def read_body(self):
        length = int(self.headers.get("Content-Length") or 0)
        return self.rfile.read(length) if length else b""

    def log_message(self, fmt, *args):
        print("[mock]", self.command, self.path, flush=True)

    # ---------- GET ----------
    def do_GET(self):
        url = urlparse(self.path)
        path = url.path
        time.sleep(0.25)  # زمن استجابة واقعي بسيط
        if path.startswith("/images/"):
            file = os.path.join(IMAGE_DIR, os.path.basename(path))
            if os.path.exists(file):
                with open(file, "rb") as f:
                    return self.send(200, f.read(), "image/jpeg")
            return self.send(404, {"detail": "image not found"})
        if path == "/parse-token":
            return self.send(200, ADMIN)
        if path == "/getAllUsers":
            return self.send(200, STORE.users)
        if path == "/api/product/getAllProducts":
            return self.send(200, [STORE.product_json(p, self.base(), False) for p in STORE.products])
        if path == "/api/product/getCategories":
            return self.send(200, STORE.categories_json(self.base()))
        if path == "/api/order/getOrders":
            return self.send(200, STORE.orders)
        return self.send(404, {"title": "Not Found", "status": 404, "detail": path})

    # ---------- POST ----------
    def do_POST(self):
        path = urlparse(self.path).path
        body = self.read_body()
        if path == "/login":
            creds = json.loads(body or b"{}")
            if creds.get("username") and creds.get("password"):
                return self.send(200, make_token(), "text/plain")
            return self.send(401, {"title": "Unauthorized", "status": 401, "detail": "Authentication failed"})
        if path == "/api/product/addProduct":
            dto = parse_multipart_json(body, self.headers.get("Content-Type"), "product") or {}
            product = {"name": dto.get("name", ""), "barcode": dto.get("barcode", ""), "price": dto.get("price", 0),
                       "quantity": dto.get("quantity", 0), "categoryId": dto.get("categoryId"),
                       "weight": dto.get("weight", 0), "materials": dto.get("materials") or [],
                       "images": ["ring_gold"], "description": dto.get("description")}
            STORE.products.append(product)
            return self.send(200, STORE.product_json(product, self.base(), False))
        if path == "/api/product/addCategory":
            dto = parse_multipart_json(body, self.headers.get("Content-Type"), "category") or {}
            cat = {"id": max(c["id"] for c in CATEGORIES) + 1, "name": dto.get("name", ""), "image": "cat_rings"}
            CATEGORIES.append(cat)
            return self.send(200, {"id": cat["id"], "name": cat["name"], "imageUrl": f"{self.base()}/images/cat_rings.jpg"})
        return self.send(404, {"detail": path})

    # ---------- PUT ----------
    def do_PUT(self):
        url = urlparse(self.path)
        body = self.read_body()
        m = re.fullmatch(r"/api/order/(\d+)/status", url.path)
        if m:
            status = parse_qs(url.query).get("status", ["CREATED"])[0]
            for order in STORE.orders:
                if order["id"] == int(m.group(1)):
                    order["status"] = status
                    return self.send(200, order)
            return self.send(404, {"detail": "Order not found"})
        m = re.fullmatch(r"/api/product/updateProduct/(.+)", url.path)
        if m:
            barcode = unquote(m.group(1))
            dto = parse_multipart_json(body, self.headers.get("Content-Type"), "product") or {}
            for p in STORE.products:
                if p["barcode"] == barcode:
                    for key in ("name", "barcode", "price", "quantity", "weight", "materials", "description"):
                        if key in dto:
                            p[key] = dto[key]
                    if dto.get("categoryId"):
                        p["categoryId"] = dto["categoryId"]
                    return self.send(200, STORE.product_json(p, self.base(), False))
            return self.send(404, {"detail": "Product does not exist"})
        return self.send(404, {"detail": url.path})

    # ---------- DELETE ----------
    def do_DELETE(self):
        path = urlparse(self.path).path
        m = re.fullmatch(r"/api/order/(\d+)", path)
        if m:
            STORE.orders = [o for o in STORE.orders if o["id"] != int(m.group(1))]
            return self.send(204)
        m = re.fullmatch(r"/api/product/deleteProduct/(.+)", path)
        if m:
            STORE.products = [p for p in STORE.products if p["barcode"] != unquote(m.group(1))]
            return self.send(200)
        return self.send(404, {"detail": path})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()
    demo_images.generate(IMAGE_DIR)
    print(f"[mock] images in {IMAGE_DIR}; listening on :{args.port}", flush=True)
    ThreadingHTTPServer(("0.0.0.0", args.port), Handler).serve_forever()


if __name__ == "__main__":
    main()

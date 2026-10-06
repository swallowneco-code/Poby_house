"""문제지 생성용 공용 도구: 수식 표기 헬퍼와 SVG 도형 그리기."""
import math
import re

SCALE = 1.3  # 그림 전체 확대 비율

# ---------------------------------------------------------------- 수식 표기

def ov(s):
    """선분 표기 (윗줄)."""
    return f'<span class="ov">{s}</span>'


def fr(a, b):
    """분수."""
    return f'<span class="fr"><span>{a}</span><span>{b}</span></span>'


def v(s):
    """이탤릭 변수."""
    return f'<i class="var">{s}</i>'


def sv(s):
    """SVG 안에서 쓰는 이탤릭 변수."""
    return f'<tspan font-style="italic">{s}</tspan>'


def tri(s):
    return f'△{s}'


def ang(s):
    return f'∠{s}'


def sq(s):
    return f'□{s}'


def sup(s):
    return f'<sup>{s}</sup>'


CM2 = 'cm' + sup(2)
CM3 = 'cm' + sup(3)
DEG = '°'
PAR = '<span class="par">//</span>'
PERP = '⊥'
CONG = '≡'
SIM = '∽'
CIRCLED = ['①', '②', '③', '④', '⑤']


def sqrt(s):
    return f'√<span class="ov">{s}</span>'


# ---------------------------------------------------------------- SVG 도형

def _add(p, q):
    return (p[0] + q[0], p[1] + q[1])


def _sub(p, q):
    return (p[0] - q[0], p[1] - q[1])


def _mul(p, k):
    return (p[0] * k, p[1] * k)


def _len(p):
    return math.hypot(p[0], p[1])


def _unit(p):
    l = _len(p)
    return (p[0] / l, p[1] / l) if l else (0.0, 0.0)


def lerp(p, q, t):
    return (p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t)


def polar(r, deg, o=(0, 0)):
    a = math.radians(deg)
    return (o[0] + r * math.cos(a), o[1] + r * math.sin(a))


def dist(p, q):
    return _len(_sub(p, q))


def foot(p, a, b):
    """점 p 에서 직선 ab 에 내린 수선의 발."""
    d = _sub(b, a)
    t = ((p[0] - a[0]) * d[0] + (p[1] - a[1]) * d[1]) / (d[0] ** 2 + d[1] ** 2)
    return (a[0] + d[0] * t, a[1] + d[1] * t)


def inter(p1, p2, p3, p4):
    """직선 p1p2 와 직선 p3p4 의 교점."""
    x1, y1 = p1
    x2, y2 = p2
    x3, y3 = p3
    x4, y4 = p4
    den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
    a = x1 * y2 - y1 * x2
    b = x3 * y4 - y3 * x4
    return ((a * (x3 - x4) - (x1 - x2) * b) / den, (a * (y3 - y4) - (y1 - y2) * b) / den)


def reflect(p, a, b):
    f = foot(p, a, b)
    return (2 * f[0] - p[0], 2 * f[1] - p[1])


def centroid(*ps):
    return (sum(p[0] for p in ps) / len(ps), sum(p[1] for p in ps) / len(ps))


def incenter(a, b, c):
    la, lb, lc = dist(b, c), dist(c, a), dist(a, b)
    s = la + lb + lc
    return ((la * a[0] + lb * b[0] + lc * c[0]) / s, (la * a[1] + lb * b[1] + lc * c[1]) / s)


def circumcenter(a, b, c):
    ax, ay = a
    bx, by = b
    cx, cy = c
    d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by))
    ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d
    uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d
    return (ux, uy)


def tri_sss(a, b, c, base_angle=0):
    """BC=a, CA=b, AB=c 인 삼각형. B=(0,0), C=(a,0), A 위쪽. (A, B, C) 반환."""
    x = (c * c - b * b + a * a) / (2 * a)
    y = math.sqrt(max(c * c - x * x, 0))
    return (x, y), (0.0, 0.0), (float(a), 0.0)


class Fig:
    """수학 좌표계(y 위쪽)로 그리고, 화면에 맞춰 자동 축소/이동한다."""

    def __init__(self, width=150, maxh=150, pad=17):
        self.width = width
        self.maxh = maxh
        self.pad = pad
        self.items = []  # (kind, data)
        self.pts = []    # bbox 계산용

    # 내부: 모든 요소는 수학 좌표로 저장, render 시 변환
    def _track(self, *ps):
        self.pts.extend(ps)

    def seg(self, p, q, dash=False, w=1.1, color='#222'):
        self._track(p, q)
        self.items.append(('line', (p, q, dash, w, color)))
        return self

    def segs(self, *ps, close=False, **kw):
        for i in range(len(ps) - 1):
            self.seg(ps[i], ps[i + 1], **kw)
        if close:
            self.seg(ps[-1], ps[0], **kw)
        return self

    def poly(self, ps, fill='#fde2c8', stroke=None, w=1.1, dash=False, opacity=1.0):
        self._track(*ps)
        self.items.append(('poly', (list(ps), fill, stroke, w, dash, opacity)))
        return self

    def path(self, d_fn, track=(), fill='none', stroke='#222', w=1.1, dash=False):
        """d_fn(T) -> svg path 문자열. T 는 좌표 변환 함수."""
        self._track(*track)
        self.items.append(('path', (d_fn, fill, stroke, w, dash)))
        return self

    def circle(self, c, r, fill='none', stroke='#222', w=1.1, dash=False):
        self._track((c[0] - r, c[1] - r), (c[0] + r, c[1] + r))
        self.items.append(('circle', (c, r, fill, stroke, w, dash)))
        return self

    def dot(self, p, r=1.8):
        self._track(p)
        self.items.append(('dot', (p, r)))
        return self

    def text(self, p, s, size=11, italic=False, anchor='middle', color='#222', bold=False):
        self._track(p)
        self.items.append(('text', (p, s, size, italic, anchor, color, bold)))
        return self

    def frac(self, p, num, den, unit='', size=9):
        """그림 안의 분수 (p 는 분수선 중심, 수학 좌표)."""
        self._track(p)
        self.items.append(('frac', (p, num, den, unit, size)))
        return self

    def label(self, p, s, away=None, dist_px=8, size=11, italic=False, dirdeg=None, color='#222'):
        """점 이름. away 점의 반대 방향(또는 dirdeg 방향)으로 dist_px 만큼 띄운다."""
        self._track(p)
        self.items.append(('label', (p, s, away, dist_px, size, italic, dirdeg, color)))
        return self

    def right(self, p, a, b, size=6):
        """점 p 에서 pa, pb 사이의 직각 표시 (size 는 px)."""
        self.items.append(('right', (p, a, b, size)))
        return self

    def arc(self, p, a, b, r=10, fill=None, stroke='#222', label=None, lsize=10, ldist=None, italic=False, double=False):
        """점 p 에서 pa→pb 방향 각 표시(작은 쪽). r, ldist 는 px."""
        self.items.append(('arc', (p, a, b, r, fill, stroke, label, lsize, ldist, italic, double)))
        return self

    def tick(self, p, q, n=1, size=4):
        self.items.append(('tick', (p, q, n, size)))
        return self

    def arrow_mark(self, p, q, n=1, t=0.5, size=4):
        """평행 표시(화살표 모양)."""
        self.items.append(('amark', (p, q, n, t, size)))
        return self

    def dim(self, p, q, s, side=1, off=9, size=10, curve=False, italic=False, t=0.5):
        """선분 pq 옆에 길이 표시. side*off 가 양수이면 p→q 진행방향 왼쪽, 음수이면 오른쪽."""
        self.items.append(('dim', (p, q, s, side, off, size, curve, italic, t)))
        return self

    # ------------------------------------------------------------ 렌더
    def render(self):
        xs = [p[0] for p in self.pts]
        ys = [p[1] for p in self.pts]
        minx, maxx, miny, maxy = min(xs), max(xs), min(ys), max(ys)
        spanx = max(maxx - minx, 1e-9)
        spany = max(maxy - miny, 1e-9)
        pad = self.pad
        k = (self.width - 2 * pad) / spanx
        if spany * k > self.maxh - 2 * pad:
            k = (self.maxh - 2 * pad) / spany
        W = spanx * k + 2 * pad
        H = spany * k + 2 * pad

        def T(p):
            return (pad + (p[0] - minx) * k, pad + (maxy - p[1]) * k)

        out = []
        cx, cy = centroid(*[T(p) for p in self.pts]) if self.pts else (0, 0)
        for kind, d in self.items:
            if kind == 'line':
                p, q, dash, w, color = d
                a, b = T(p), T(q)
                da = ' stroke-dasharray="3,2.5"' if dash else ''
                out.append(f'<line x1="{a[0]:.2f}" y1="{a[1]:.2f}" x2="{b[0]:.2f}" y2="{b[1]:.2f}" stroke="{color}" stroke-width="{w}"{da}/>')
            elif kind == 'poly':
                ps, fill, stroke, w, dash, op = d
                pts = ' '.join(f'{T(p)[0]:.2f},{T(p)[1]:.2f}' for p in ps)
                st = f' stroke="{stroke}" stroke-width="{w}"' if stroke else ' stroke="none"'
                da = ' stroke-dasharray="3,2.5"' if dash else ''
                out.append(f'<polygon points="{pts}" fill="{fill}" fill-opacity="{op}"{st}{da}/>')
            elif kind == 'path':
                fn, fill, stroke, w, dash = d
                da = ' stroke-dasharray="3,2.5"' if dash else ''
                out.append(f'<path d="{fn(T, k)}" fill="{fill}" stroke="{stroke}" stroke-width="{w}"{da}/>')
            elif kind == 'circle':
                c, r, fill, stroke, w, dash = d
                a = T(c)
                da = ' stroke-dasharray="3,2.5"' if dash else ''
                out.append(f'<circle cx="{a[0]:.2f}" cy="{a[1]:.2f}" r="{r * k:.2f}" fill="{fill}" stroke="{stroke}" stroke-width="{w}"{da}/>')
            elif kind == 'dot':
                p, r = d
                a = T(p)
                out.append(f'<circle cx="{a[0]:.2f}" cy="{a[1]:.2f}" r="{r}" fill="#222"/>')
            elif kind == 'text':
                p, s, size, italic, anchor, color, bold = d
                a = T(p)
                st = ' font-style="italic"' if italic else ''
                bw = ' font-weight="bold"' if bold else ''
                out.append(f'<text x="{a[0]:.2f}" y="{a[1] + size * 0.35:.2f}" font-size="{size}" text-anchor="{anchor}" fill="{color}"{st}{bw}>{s}</text>')
            elif kind == 'frac':
                p, num, den, unit, size = d
                a = T(p)
                wdt = max(len(str(num)), len(str(den))) * size * 0.55 + 2
                out.append(f'<text x="{a[0]:.2f}" y="{a[1] - 2:.2f}" font-size="{size}" text-anchor="middle">{num}</text>')
                out.append(f'<line x1="{a[0] - wdt / 2:.2f}" y1="{a[1]:.2f}" x2="{a[0] + wdt / 2:.2f}" y2="{a[1]:.2f}" stroke="#222" stroke-width="0.7"/>')
                out.append(f'<text x="{a[0]:.2f}" y="{a[1] + size * 0.95:.2f}" font-size="{size}" text-anchor="middle">{den}</text>')
                if unit:
                    out.append(f'<text x="{a[0] + wdt / 2 + 1:.2f}" y="{a[1] + size * 0.35:.2f}" font-size="{size}" text-anchor="start">{unit}</text>')
            elif kind == 'label':
                p, s, away, dpx, size, italic, dirdeg, color = d
                a = T(p)
                if dirdeg is not None:
                    u = (math.cos(math.radians(dirdeg)), -math.sin(math.radians(dirdeg)))
                else:
                    ref = T(away) if away is not None else (cx, cy)
                    u = _unit(_sub(a, ref))
                    if u == (0.0, 0.0):
                        u = (0, -1)
                x = a[0] + u[0] * dpx
                y = a[1] + u[1] * dpx + size * 0.35
                st = ' font-style="italic"' if italic else ''
                out.append(f'<text x="{x:.2f}" y="{y:.2f}" font-size="{size}" text-anchor="middle" fill="{color}"{st}>{s}</text>')
            elif kind == 'right':
                p, a, b, size = d
                P, A, B = T(p), T(a), T(b)
                u1 = _unit(_sub(A, P))
                u2 = _unit(_sub(B, P))
                q1 = _add(P, _mul(u1, size))
                q2 = _add(P, _mul(u2, size))
                q3 = _add(q1, _mul(u2, size))
                out.append(f'<polyline points="{q1[0]:.2f},{q1[1]:.2f} {q3[0]:.2f},{q3[1]:.2f} {q2[0]:.2f},{q2[1]:.2f}" fill="none" stroke="#222" stroke-width="0.8"/>')
            elif kind == 'arc':
                p, a, b, r, fill, stroke, lab, lsize, ldist, italic, double = d
                P, A, B = T(p), T(a), T(b)
                a1 = math.atan2(A[1] - P[1], A[0] - P[0])
                a2 = math.atan2(B[1] - P[1], B[0] - P[0])
                diff = (a2 - a1) % (2 * math.pi)
                sweep = 1
                if diff > math.pi:
                    a1, a2 = a2, a1
                    diff = 2 * math.pi - diff
                s1 = (P[0] + r * math.cos(a1), P[1] + r * math.sin(a1))
                s2 = (P[0] + r * math.cos(a1 + diff), P[1] + r * math.sin(a1 + diff))
                if fill:
                    out.append(f'<path d="M{P[0]:.2f},{P[1]:.2f} L{s1[0]:.2f},{s1[1]:.2f} A{r},{r} 0 0 {sweep} {s2[0]:.2f},{s2[1]:.2f} Z" fill="{fill}" stroke="none"/>')
                out.append(f'<path d="M{s1[0]:.2f},{s1[1]:.2f} A{r},{r} 0 0 {sweep} {s2[0]:.2f},{s2[1]:.2f}" fill="none" stroke="{stroke}" stroke-width="0.8"/>')
                if double:
                    r2 = r + 2.5
                    t1 = (P[0] + r2 * math.cos(a1), P[1] + r2 * math.sin(a1))
                    t2 = (P[0] + r2 * math.cos(a1 + diff), P[1] + r2 * math.sin(a1 + diff))
                    out.append(f'<path d="M{t1[0]:.2f},{t1[1]:.2f} A{r2},{r2} 0 0 {sweep} {t2[0]:.2f},{t2[1]:.2f}" fill="none" stroke="{stroke}" stroke-width="0.8"/>')
                if lab:
                    mid = a1 + diff / 2
                    ld = ldist if ldist is not None else r + 8
                    x = P[0] + ld * math.cos(mid)
                    y = P[1] + ld * math.sin(mid) + lsize * 0.35
                    st = ' font-style="italic"' if italic else ''
                    out.append(f'<text x="{x:.2f}" y="{y:.2f}" font-size="{lsize}" text-anchor="middle"{st}>{lab}</text>')
            elif kind == 'tick':
                p, q, n, size = d
                P, Q = T(p), T(q)
                m = lerp(P, Q, 0.5)
                u = _unit(_sub(Q, P))
                nrm = (-u[1], u[0])
                for i in range(n):
                    off = (i - (n - 1) / 2) * 2.6
                    c = _add(m, _mul(u, off))
                    e1 = _add(c, _mul(nrm, size))
                    e2 = _sub(c, _mul(nrm, size))
                    out.append(f'<line x1="{e1[0]:.2f}" y1="{e1[1]:.2f}" x2="{e2[0]:.2f}" y2="{e2[1]:.2f}" stroke="#222" stroke-width="0.8"/>')
            elif kind == 'amark':
                p, q, n, t, size = d
                P, Q = T(p), T(q)
                u = _unit(_sub(Q, P))
                nrm = (-u[1], u[0])
                for i in range(n):
                    c = _add(lerp(P, Q, t), _mul(u, (i - (n - 1) / 2) * 3.2))
                    tip = _add(c, _mul(u, size * 0.6))
                    b1 = _add(_sub(c, _mul(u, size * 0.6)), _mul(nrm, size * 0.8))
                    b2 = _sub(_sub(c, _mul(u, size * 0.6)), _mul(nrm, size * 0.8))
                    out.append(f'<polyline points="{b1[0]:.2f},{b1[1]:.2f} {tip[0]:.2f},{tip[1]:.2f} {b2[0]:.2f},{b2[1]:.2f}" fill="none" stroke="#222" stroke-width="0.9"/>')
            elif kind == 'dim':
                p, q, s, side, off, size, curve, italic, tt = d
                P, Q = T(p), T(q)
                u = _unit(_sub(Q, P))
                nrm = (u[1], -u[0])  # 화면좌표에서 진행방향 왼쪽
                nrm = _mul(nrm, side)
                m = lerp(P, Q, tt)
                st = ' font-style="italic"' if italic else ''
                if curve:
                    bulge = off * 1.1
                    c = _add(lerp(P, Q, 0.5), _mul(nrm, bulge * 2))
                    out.append(f'<path d="M{P[0]:.2f},{P[1]:.2f} Q{c[0]:.2f},{c[1]:.2f} {Q[0]:.2f},{Q[1]:.2f}" fill="none" stroke="#666" stroke-width="0.7" stroke-dasharray="2,2"/>')
                    tp = _add(m, _mul(nrm, bulge + math.copysign(size * 0.6, bulge)))
                else:
                    tp = _add(m, _mul(nrm, off))
                out.append(f'<text x="{tp[0]:.2f}" y="{tp[1] + size * 0.35:.2f}" font-size="{size}" text-anchor="middle" fill="#222"{st}>{s}</text>')
        body = '\n'.join(out)
        # 글자가 그림 밖으로 잘리지 않도록 viewBox 를 글자 영역까지 넓힌다.
        x0, y0, x1, y1 = 0.0, 0.0, W, H
        for m in re.finditer(r'<text x="([-\d.]+)" y="([-\d.]+)" font-size="([\d.]+)" text-anchor="(\w+)"[^>]*>(.*?)</text>', body):
            x, y, fs, anc, txt = float(m.group(1)), float(m.group(2)), float(m.group(3)), m.group(4), m.group(5)
            n = len(re.sub(r'<[^>]+>', '', txt))
            tw = n * fs * 0.52
            lx = x - tw / 2 if anc == 'middle' else (x if anc == 'start' else x - tw)
            x0, x1 = min(x0, lx - 2), max(x1, lx + tw + 2)
            y0, y1 = min(y0, y - fs - 1), max(y1, y + fs * 0.3 + 1)
        vw, vh = x1 - x0, y1 - y0
        return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{vw * SCALE:.1f}" height="{vh * SCALE:.1f}" '
                f'viewBox="{x0:.1f} {y0:.1f} {vw:.1f} {vh:.1f}" font-family="Liberation Serif, Times New Roman, serif">{body}</svg>')


def P(question, choices=None, fig=None, figpos='right', ans='', sol='', tag=None, figw=None):
    """문제 하나."""
    return dict(q=question, ch=choices, fig=fig, figpos=figpos, ans=ans, sol=sol, tag=tag, figw=figw)


def arcpts(c, r, a0, a1, n=48):
    """중심 c, 반지름 r, 각 a0→a1(도) 을 따라가는 점 목록."""
    return [polar(r, a0 + (a1 - a0) * i / n, c) for i in range(n + 1)]


def ellipse(fig, c, rx, ry, back_dash=True, w=1.1):
    """밑면 타원. 뒤쪽(위) 절반은 점선."""
    def front(T, k):
        x, y = T(c)
        return f'M{x - rx * k:.2f},{y:.2f} A{rx * k:.2f},{ry * k:.2f} 0 0 0 {x + rx * k:.2f},{y:.2f}'

    def back(T, k):
        x, y = T(c)
        return f'M{x - rx * k:.2f},{y:.2f} A{rx * k:.2f},{ry * k:.2f} 0 0 1 {x + rx * k:.2f},{y:.2f}'
    fig.path(front, track=[(c[0] - rx, c[1] - ry), (c[0] + rx, c[1] + ry)], w=w)
    fig.path(back, dash=back_dash, w=w)
    return fig


def solve(f, lo, hi, it=200):
    """f(lo), f(hi) 부호가 다를 때 이분법."""
    flo = f(lo)
    for _ in range(it):
        mid = (lo + hi) / 2
        fm = f(mid)
        if (fm > 0) == (flo > 0):
            lo, flo = mid, fm
        else:
            hi = mid
    return (lo + hi) / 2


def outer_square(p, q, away):
    """선분 pq 를 한 변으로 하고 away 점의 반대쪽에 그린 정사각형의 나머지 두 꼭짓점 (p쪽, q쪽)."""
    d = _sub(q, p)
    n = (-d[1], d[0])
    m = lerp(p, q, 0.5)
    if dist(_add(m, n), away) < dist(_sub(m, n), away):
        n = (-n[0], -n[1])
    return _add(p, n), _add(q, n)

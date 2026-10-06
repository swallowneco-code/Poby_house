"""3. 도형의 닮음"""
import math
from common import *  # noqa: F401,F403

PROBLEMS = []
add = PROBLEMS.append

OB = (0.55, 0.32)  # 사영 방향(깊이 1 당 화면 이동)


def cuboid(f, o, w, h, d, names=None):
    """o: 앞면 왼쪽 아래. names: 위 4개(뒤왼, 앞왼, 앞오, 뒤오) + 아래 4개(같은 순서)."""
    dx, dy = OB[0] * d, OB[1] * d
    F_ = o
    G_ = (o[0] + w, o[1])
    B_ = (o[0], o[1] + h)
    C_ = (o[0] + w, o[1] + h)
    E_ = (o[0] + dx, o[1] + dy)
    H_ = (G_[0] + dx, G_[1] + dy)
    A_ = (B_[0] + dx, B_[1] + dy)
    D_ = (C_[0] + dx, C_[1] + dy)
    f.segs(B_, C_, G_, F_, close=True).segs(B_, A_, D_, C_).segs(D_, H_, G_)
    f.seg(F_, E_, dash=True).seg(E_, H_, dash=True).seg(E_, A_, dash=True)
    pts = [A_, B_, C_, D_, E_, F_, G_, H_]
    if names:
        dirs = [120, 150, 60, 45, 200, 225, -45, -20]
        for p, n, dd in zip(pts, names, dirs):
            f.label(p, n, dirdeg=dd, size=10)
    return pts


def prism(f, o, w, h, back, names=None):
    """삼각기둥. 아래 삼각형 E(앞왼) F(앞오) D(뒤), 위 B C A."""
    E_ = o
    F_ = (o[0] + w, o[1])
    D_ = (o[0] + back[0], o[1] + back[1])
    B_ = (E_[0], E_[1] + h)
    C_ = (F_[0], F_[1] + h)
    A_ = (D_[0], D_[1] + h)
    f.segs(A_, B_, C_, close=True).seg(B_, E_).seg(C_, F_).seg(E_, F_)
    f.seg(D_, E_, dash=True).seg(D_, F_, dash=True).seg(A_, D_, dash=True)
    pts = [A_, B_, C_, D_, E_, F_]
    if names:
        dirs = [90, 180, 0, 160, 225, -45]
        for p, n, dd in zip(pts, names, dirs):
            f.label(p, n, dirdeg=dd, size=10)
    return pts


def cone(f, apex_h, r, cx=0.0, ry_ratio=0.28):
    T = (cx, apex_h)
    O = (cx, 0.0)
    f.seg(T, (cx - r, 0)).seg(T, (cx + r, 0))
    ellipse(f, O, r, r * ry_ratio)
    return T, O


# 01 ------------------------------------------------------------------
add(P('다음 중 항상 닮은 도형인 것을 모두 고르면? (정답 2개)',
      ['두 직사각형', '두 원뿔', '두 정육면체', '두 이등변삼각형', '두 직각이등변삼각형'],
      ans='③, ⑤',
      sol='<p>정육면체는 모두 모양이 같으므로 항상 닮은 도형이다.</p>'
          '<p>직각이등변삼각형은 세 각의 크기가 90°, 45°, 45°로 항상 같으므로 항상 닮은 도형이다.</p>'
          '<p>①, ②, ④는 가로와 세로의 비, 밑면의 반지름과 높이의 비, 꼭지각의 크기 등이 다를 수 있으므로 항상 닮은 도형은 아니다.</p>'))

# 02 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (12.0, 0.0)
A = polar(9, 75)
D = foot(A, C, polar(1, 100, C))
k = 2 / 3
sh = 16.0
Pq = [(sh + p[0] * k, p[1] * k) for p in (A, B, C, D)]
Pp, Q, R, S = Pq
f = Fig(190, 95)
f.segs(A, B, C, D, close=True).segs(Pp, Q, R, S, close=True)
f.arc(B, C, A, 10, label='75°', lsize=8, ldist=20).arc(C, D, B, 10, label='80°', lsize=8, ldist=20).right(D, A, C, 5)
f.dim(B, A, '9', side=1, off=9, size=9).dim(B, C, '12', side=1, off=-9, size=9).dim(Q, R, '8', side=1, off=-9, size=9)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(Pp, 'P', dirdeg=120).label(Q, 'Q', dirdeg=225).label(R, 'R', dirdeg=-45).label(S, 'S', dirdeg=60)
add(P(f'아래 그림에서 {sq("ABCD")}{SIM}{sq("PQRS")}일 때, 다음 중 옳지 <u>않은</u> 것은?',
      ['닮음비는 3 : 2이다.', f'{ang("P")}=115°', f'{ang("S")}=90°', f'{ov("PQ")}=6', f'{ov("CD")} : {ov("RS")}=2 : 3'],
      f.render(), figpos='below',
      ans='⑤',
      sol=f'<p>① 닮음비는 {ov("BC")} : {ov("QR")}=12 : 8=3 : 2</p>'
          f'<p>② {ang("P")}={ang("A")}=360°−(75°+80°+90°)=115°</p><p>③ {ang("S")}={ang("D")}=90°</p>'
          f'<p>④ 9 : {ov("PQ")}=3 : 2 ∴ {ov("PQ")}=6</p>'
          f'<p>⑤ {ov("CD")} : {ov("RS")}=3 : 2이므로 옳지 않다.</p>'))

# 03 ------------------------------------------------------------------
f = Fig(200, 120)
s1 = 0.4
pa = prism(f, (0, 0), 8 * s1, 10 * s1, (1.4, 1.4), names=['A', 'B', 'C', 'D', 'E', 'F'])
A1, B1, C1, D1, E1, F1 = pa
f.dim(B1, A1, '4 cm', side=1, off=9, size=8.5).dim(E1, F1, '8 cm', side=1, off=-9, size=8.5)
f.dim(F1, C1, f'{sv("x")} cm', side=1, off=-12, size=8.5)
s2 = 0.6
pb = prism(f, (6.5, 0), 8 * s1 * 1.5, 10 * s1 * 1.5, (2.1, 2.1), names=['G', 'H', 'I', 'J', 'K', 'L'])
G1, H1, I1, J1, K1, L1 = pb
f.dim(H1, G1, '6 cm', side=1, off=9, size=8.5).dim(K1, L1, f'{sv("y")} cm', side=1, off=-9, size=8.5)
f.dim(L1, I1, '15 cm', side=1, off=-14, size=8.5)
add(P(f'다음 그림에서 두 삼각기둥은 서로 닮은 도형이고 {ov("AB")}에 대응하는 모서리가 {ov("GH")}일 때, {v("x")}+{v("y")}의 값을 구하시오.',
      None, f.render(), figpos='below',
      ans='22',
      sol=f'<p>닮음비는 {ov("AB")} : {ov("GH")}=4 : 6=2 : 3</p>'
          f'<p>{ov("CF")} : {ov("IL")}=2 : 3에서 {v("x")} : 15=2 : 3 ∴ {v("x")}=10</p>'
          f'<p>{ov("EF")} : {ov("KL")}=2 : 3에서 8 : {v("y")}=2 : 3 ∴ {v("y")}=12</p><p>∴ {v("x")}+{v("y")}=22</p>'))

# 04 ------------------------------------------------------------------
f = Fig(190, 120)
Ts, Os = cone(f, 5, 3, cx=0)
f.seg(Ts, Os, dash=True).right(Os, Ts, (3, 0), 4)
f.dim(Ts, Os, '5 cm', side=1, off=-12, size=8.5, t=0.45)
Tb, Ob = cone(f, 10, 6, cx=12)
f.seg(Tb, Ob, dash=True).seg(Ob, (18, 0), dash=True).right(Ob, Tb, (18, 0), 5)
f.text((15, -2.4), '6 cm', size=8.5)
add(P(f'다음 그림에서 두 원뿔은 서로 닮은 도형이다. 작은 원뿔의 밑면의 둘레의 길이가 6π cm일 때, 큰 원뿔의 높이를 구하시오.',
      None, f.render(), figpos='below',
      ans='10 cm',
      sol=f'<p>작은 원뿔의 밑면의 반지름의 길이를 <i class="var">r</i> cm라고 하면 2π<i class="var">r</i>=6π ∴ <i class="var">r</i>=3</p>'
          f'<p>닮음비는 3 : 6=1 : 2이므로 큰 원뿔의 높이를 <i class="var">h</i> cm라고 하면</p>'
          f'<p>5 : <i class="var">h</i>=1 : 2 ∴ <i class="var">h</i>=10</p>'))

# 05 ------------------------------------------------------------------
add(P(f'두 정삼각형 ABC와 DEF의 한 변의 길이의 비가 4 : 3이고 {tri("ABC")}의 넓이가 48 {CM2}일 때, {tri("DEF")}의 넓이를 구하시오.',
      None,
      ans='27 ' + CM2,
      sol=f'<p>두 정삼각형은 닮은 도형이고 닮음비가 4 : 3이므로 넓이의 비는 4<sup>2</sup> : 3<sup>2</sup>=16 : 9</p>'
          f'<p>48 : {tri("DEF")}=16 : 9 ∴ {tri("DEF")}=27({CM2})</p>'))

# 06 ------------------------------------------------------------------
f = Fig(170, 90)
cuboid(f, (0, 0), 3, 2.4, 2.2)
cuboid(f, (6.2, 0), 4, 3.2, 2.9)
f.text((2.0, -1.2), 'A', size=10).text((8.8, -1.2), 'B', size=10)
add(P(f'다음 그림에서 두 직육면체 A, B는 서로 닮은 도형이고, 부피의 비가 27 : 64이다. 직육면체 A의 겉넓이가 36 {CM2}일 때, 직육면체 B의 겉넓이를 구하시오.',
      None, f.render(), figpos='below',
      ans='64 ' + CM2,
      sol=f'<p>부피의 비가 27 : 64=3<sup>3</sup> : 4<sup>3</sup>이므로 닮음비는 3 : 4</p>'
          f'<p>겉넓이의 비는 3<sup>2</sup> : 4<sup>2</sup>=9 : 16</p>'
          f'<p>36 : (B의 겉넓이)=9 : 16 ∴ (B의 겉넓이)=64({CM2})</p>'))

# 07 ------------------------------------------------------------------
add(P('반지름의 길이가 6 cm인 구 모양의 쇠구슬 1개를 녹여서 반지름의 길이가 2 cm인 구 모양의 쇠구슬을 최대 몇 개 만들 수 있는가?',
      ['9개', '18개', '27개', '36개', '54개'],
      ans='③',
      sol='<p>두 구의 닮음비는 6 : 2=3 : 1이므로 부피의 비는 3<sup>3</sup> : 1<sup>3</sup>=27 : 1</p>'
          '<p>따라서 작은 쇠구슬을 최대 27개 만들 수 있다.</p>'))

# 08 ------------------------------------------------------------------
f = Fig(90, 110)
H = 8.0
R = 3.6
Tt = (0, 0)
f.seg(Tt, (-R, H)).seg(Tt, (R, H))
ellipse(f, (0, H), R, R * 0.28, back_dash=False)
h2 = H * 3 / 4
r2 = R * 3 / 4
f.poly([Tt, (-r2, h2), (r2, h2)], fill='#bfe3ef', stroke=None)
ellipse(f, (0, h2), r2, r2 * 0.28, back_dash=True, w=0.8)
f.seg(Tt, (-R, H)).seg(Tt, (R, H))
add(P(f'오른쪽 그림과 같이 원뿔 모양의 그릇에 일정한 속도로 물을 채우고 있다. 물을 전체 높이의 {fr(3, 4)}만큼 채우는 데 27초가 걸렸다면 그릇에 물을 가득 채울 때까지 몇 초가 더 걸리는지 구하시오. (단, 그릇의 두께는 무시한다.)',
      None, f.render(),
      ans='37초',
      sol=f'<p>물이 채워진 부분과 그릇은 닮은 원뿔이고 닮음비는 3 : 4이므로 부피의 비는 3<sup>3</sup> : 4<sup>3</sup>=27 : 64</p>'
          f'<p>27초 동안 27만큼 채웠으므로 1만큼 채우는 데 1초가 걸린다.</p>'
          f'<p>남은 부피는 64−27=37이므로 37초가 더 걸린다.</p>'))

# 09 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (8.0, 0.0)
A = polar(8 * math.cos(math.radians(30)), 30)
f = Fig(130, 80)
f.segs(A, B, C, close=True).right(A, B, C, 5)
f.arc(B, C, A, 14, label='30°', lsize=8.5, ldist=26)
f.dim(A, C, '4 cm', side=1, off=-10, size=8.5).dim(B, C, '8 cm', side=1, off=-10, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
add(P(f'다음 중 오른쪽 그림의 {tri("ABC")}와 닮은 삼각형을 모두 고르면? (정답 2개)',
      [f'{ang("D")}=60°, {ang("E")}=80°인 {tri("DEF")}',
       f'{ov("MN")}=2 cm, {ov("NO")}=4 cm, {ang("N")}=60°인 {tri("MNO")}',
       f'{ang("J")}=60°, {ang("K")}=90°인 {tri("JKL")}',
       f'{ang("P")}=30°, {ang("Q")}=50°인 {tri("PQR")}',
       f'{ang("S")}=90°, {ang("T")}=45°인 {tri("STU")}'],
      f.render(),
      ans='②, ③',
      sol=f'<p>{tri("ABC")}에서 {ang("C")}=180°−(90°+30°)=60°</p>'
          f'<p>② {ov("NM")} : {ov("CA")}=2 : 4=1 : 2, {ov("NO")} : {ov("CB")}=4 : 8=1 : 2, {ang("N")}={ang("C")}=60°이므로 SAS 닮음</p>'
          f'<p>③ {ang("J")}={ang("C")}=60°, {ang("K")}={ang("A")}=90°이므로 AA 닮음</p>'
          f'<p>① 세 각이 60°, 80°, 40° ④ 세 각이 30°, 50°, 100° ⑤ 세 각이 90°, 45°, 45°이므로 닮음이 아니다.</p>'))

# 10 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (4.0, 0.0)
A = inter(B, polar(1, 50), C, polar(1, 110, C))
E = (7.5, 0.0)
Fq = (17.5, 0.0)
D = inter(E, polar(1, 50, E), Fq, polar(1, 110, Fq))
f = Fig(190, 95)
f.segs(A, B, C, close=True).segs(D, E, Fq, close=True)
f.arc(B, C, A, 9, label='50°', lsize=8, ldist=19).arc(E, Fq, D, 10, label='50°', lsize=8, ldist=21)
f.dim(B, C, '4 cm', side=1, off=-9, size=8.5).dim(E, Fq, '10 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
f.label(D, 'D', dirdeg=90).label(E, 'E', dirdeg=200).label(Fq, 'F', dirdeg=-20)
add(P(f'아래 그림의 {tri("ABC")}와 {tri("DEF")}가 서로 닮은 도형이 되게 하려면 다음 중 어느 조건을 추가해야 하는가?',
      [f'{ang("A")}=60°, {ang("F")}=70°', f'{ov("AB")}=2 cm, {ov("DE")}=6 cm', f'{ov("AC")}=4 cm, {ov("DF")}=10 cm',
       f'{ang("C")}=60°, {ang("D")}=60°', f'{ov("AB")}=3 cm, {ov("DF")}=7.5 cm'],
      f.render(), figpos='below',
      ans='①',
      sol=f'<p>① {ang("A")}=60°이면 {ang("C")}=180°−(50°+60°)=70°={ang("F")}이고 {ang("B")}={ang("E")}이므로 AA 닮음이다.</p>'
          f'<p>② {ov("AB")} : {ov("DE")}=1 : 3, {ov("BC")} : {ov("EF")}=2 : 5로 비가 다르다.</p>'
          f'<p>③, ⑤ 끼인각이 아닌 각이 같으므로 닮음이라고 할 수 없다.</p>'
          f'<p>④ {ang("C")}=60°이면 {ang("A")}=70°이므로 {ang("D")}=60°와 대응하는 각이 같지 않다.</p>'))

# 11 ------------------------------------------------------------------
A, B, C = tri_sss(9, 8, 6)
D = (4.0, 0.0)
f = Fig(150, 90)
f.segs(A, B, C, close=True).seg(A, D)
f.dim(B, A, '6 cm', side=1, off=10, size=8.5).dim(C, A, '8 cm', side=1, off=-10, size=8.5)
f.dim(B, D, '4 cm', side=1, off=-9, size=8.5).dim(D, C, '5 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90, dist_px=15)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("AD")}의 길이는?',
      ['5 cm', f'{fr(16, 3)} cm', f'{fr(11, 2)} cm', '6 cm', f'{fr(13, 2)} cm'], f.render(),
      ans='②',
      sol=f'<p>{tri("ABC")}와 {tri("DBA")}에서 {ov("AB")} : {ov("DB")}=6 : 4=3 : 2, {ov("BC")} : {ov("BA")}=9 : 6=3 : 2, {ang("B")}는 공통</p>'
          f'<p>∴ {tri("ABC")}{SIM}{tri("DBA")} (SAS 닮음)</p>'
          f'<p>{ov("CA")} : {ov("AD")}=3 : 2에서 8 : {ov("AD")}=3 : 2 ∴ {ov("AD")}={fr(16, 3)} cm</p>'))

# 12 ------------------------------------------------------------------
A = (0.0, 0.0)
B = polar(12, 235)
C = polar(10, 300)
D = lerp(A, B, 5 / 12)
E = lerp(A, C, 6 / 10)
f = Fig(140, 110)
f.segs(A, B, C, close=True).seg(D, E)
f.arc(B, C, A, 10).arc(E, A, D, 9)
f.dim(B, A, '12 cm', side=1, off=14, size=8.5, curve=False, t=0.35).dim(A, D, '5 cm', side=1, off=-10, size=8.5).dim(A, E, '6 cm', side=1, off=10, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ang("ABC")}={ang("AED")}이고 {ov("AB")}=12 cm, {ov("AD")}=5 cm, {ov("AE")}=6 cm일 때, {ov("CE")}의 길이는?',
      ['3 cm', f'{fr(7, 2)} cm', '4 cm', f'{fr(9, 2)} cm', '5 cm'], f.render(),
      ans='③',
      sol=f'<p>{tri("ABC")}와 {tri("AED")}에서 {ang("ABC")}={ang("AED")}, {ang("A")}는 공통이므로 {tri("ABC")}{SIM}{tri("AED")} (AA 닮음)</p>'
          f'<p>{ov("AB")} : {ov("AE")}={ov("AC")} : {ov("AD")}에서 12 : 6={ov("AC")} : 5 ∴ {ov("AC")}=10 cm</p>'
          f'<p>∴ {ov("CE")}=10−6=4(cm)</p>'))

# 13 ------------------------------------------------------------------
B = (0.0, 0.0)
A = polar(6, 75)
C = (9.0, 0.0)
D = (A[0] + 9, A[1])
Fp = polar(-3, 75)
E = inter(D, Fp, B, C)
f = Fig(150, 120)
f.segs(A, B, C, D, close=True).seg(B, Fp).seg(D, Fp)
f.dim(B, A, '6 cm', side=1, off=12, size=8.5).dim(A, D, '9 cm', side=1, off=9, size=8.5).dim(Fp, B, '3 cm', side=1, off=12, size=8.5)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=160).label(C, 'C', dirdeg=-30).label(D, 'D', dirdeg=45).label(E, 'E', dirdeg=-70).label(Fp, 'F', dirdeg=225)
add(P(f'다음 그림과 같은 평행사변형 ABCD에서 {ov("AB")}의 연장선과 {ov("DE")}의 연장선이 만나는 점을 F라고 하자. {ov("AB")}=6 cm, {ov("AD")}=9 cm, {ov("BF")}=3 cm일 때, {ov("CE")}의 길이를 구하시오.',
      None, f.render(), figpos='below',
      ans='6 cm',
      sol=f'<p>{ov("BE")}{PAR}{ov("AD")}이므로 {tri("FBE")}{SIM}{tri("FAD")} (AA 닮음)</p>'
          f'<p>{ov("FB")} : {ov("FA")}={ov("BE")} : {ov("AD")}에서 3 : 9={ov("BE")} : 9 ∴ {ov("BE")}=3 cm</p>'
          f'<p>∴ {ov("CE")}={ov("BC")}−{ov("BE")}=9−3=6(cm)</p>'))

# 14 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (16.0, 0.0)
A = polar(12, math.degrees(math.acos(12 / 16)))
M = (8.0, 0.0)
D = inter(M, (8, 1), A, B)
f = Fig(150, 95)
f.segs(A, B, C, close=True).seg(D, M).right(A, B, C, 5).right(M, D, C, 5).tick(B, M, 2).tick(M, C, 2)
f.dim(B, A, '12 cm', side=1, off=12, size=8.5).dim(B, C, '16 cm', side=1, off=-12, size=8.5, curve=True)
f.label(A, 'A', dirdeg=80).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=160, dist_px=9).label(M, 'M', dirdeg=-90, dist_px=8)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 점 M은 {ov("BC")}의 중점이고, {ov("DM")}{PERP}{ov("BC")}이다. {ov("AB")}=12 cm, {ov("BC")}=16 cm일 때, {ov("BD")}의 길이는?',
      ['10 cm', f'{fr(31, 3)} cm', f'{fr(32, 3)} cm', '11 cm', f'{fr(34, 3)} cm'], f.render(),
      ans='③',
      sol=f'<p>{tri("ABC")}와 {tri("MBD")}에서 {ang("BAC")}={ang("BMD")}=90°, {ang("B")}는 공통이므로 {tri("ABC")}{SIM}{tri("MBD")} (AA 닮음)</p>'
          f'<p>{ov("AB")} : {ov("MB")}={ov("BC")} : {ov("BD")}에서 12 : 8=16 : {ov("BD")}</p>'
          f'<p>12{ov("BD")}=128 ∴ {ov("BD")}={fr(32, 3)} cm</p>'))

# 15 ------------------------------------------------------------------
A, B, C, D = (0, 10), (0, 0), (12, 0), (12, 10)
E = (12, 2)
Fq = (15, 0)
f = Fig(150, 105)
f.segs(A, B, C, D, close=True).seg(A, Fq).seg(C, Fq)
f.dim(B, A, '10 cm', side=1, off=14, size=8.5).dim(A, D, '12 cm', side=1, off=9, size=8.5)
f.dim(B, Fq, f'{sv("x")} cm', side=1, off=-10, size=8.5, curve=True)
f.dim(D, E, f'{sv("y")} cm', side=1, off=-10, size=8.5)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-60).label(D, 'D', dirdeg=45).label(E, 'E', dirdeg=160, dist_px=9).label(Fq, 'F', dirdeg=-30)
add(P(f'오른쪽 그림과 같은 직사각형 ABCD에서 {ov("CD")} 위에 한 점 E를 잡고, {ov("AE")}의 연장선과 {ov("BC")}의 연장선의 교점을 F라고 하자. {ov("AE")} : {ov("EF")}=4 : 1이고 {ov("AB")}=10 cm, {ov("AD")}=12 cm, {ov("BF")}={v("x")} cm, {ov("DE")}={v("y")} cm일 때, {v("x")}−{v("y")}의 값을 구하시오.',
      None, f.render(),
      ans='7',
      sol=f'<p>{ov("AD")}{PAR}{ov("CF")}이므로 {tri("AED")}{SIM}{tri("FEC")} (AA 닮음)이고 닮음비는 {ov("AE")} : {ov("FE")}=4 : 1</p>'
          f'<p>{ov("DE")} : {ov("CE")}=4 : 1이므로 {ov("DE")}=10×{fr(4, 5)}=8(cm) ∴ {v("y")}=8</p>'
          f'<p>{ov("AD")} : {ov("FC")}=4 : 1이므로 {ov("FC")}=3 cm, {ov("BF")}=12+3=15(cm) ∴ {v("x")}=15</p>'
          f'<p>∴ {v("x")}−{v("y")}=15−8=7</p>'))

# 16 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (10.0, 0.0)
A = (6.4, 4.8)
H = (6.4, 0.0)
f = Fig(130, 80)
f.segs(A, B, C, close=True).seg(A, H).right(A, B, C, 5).right(H, A, C, 5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(H, 'H', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 {ov("AH")}{PERP}{ov("BC")}일 때, 다음 중 옳지 <u>않은</u> 것은?',
      [f'{tri("ABC")}{SIM}{tri("HAC")}', f'{tri("HBA")}{SIM}{tri("HAC")}', f'{ov("AH")}<sup>2</sup>={ov("BH")}×{ov("CH")}',
       f'{ov("AC")}<sup>2</sup>={ov("CH")}×{ov("CB")}', f'{ov("AB")}×{ov("AC")}={ov("AH")}×{ov("BH")}'],
      f.render(),
      ans='⑤',
      sol=f'<p>①, ② 모두 AA 닮음이다.</p><p>③ {tri("HBA")}{SIM}{tri("HAC")}에서 {ov("AH")}<sup>2</sup>={ov("BH")}×{ov("CH")}</p>'
          f'<p>④ {tri("ABC")}{SIM}{tri("HAC")}에서 {ov("AC")}<sup>2</sup>={ov("CH")}×{ov("CB")}</p>'
          f'<p>⑤ 넓이를 두 가지로 나타내면 {fr(1, 2)}×{ov("AB")}×{ov("AC")}={fr(1, 2)}×{ov("BC")}×{ov("AH")}이므로 {ov("AB")}×{ov("AC")}={ov("AH")}×{ov("BC")}이다.</p>'))

# 17 ------------------------------------------------------------------
B = (0.0, 0.0)
D = (9.0, 0.0)
C = (13.0, 0.0)
A = (9.0, 6.0)
f = Fig(150, 80)
f.poly([A, B, D], fill='#f9d7e2', stroke='#222')
f.segs(A, B, C, close=True).seg(A, D).right(A, B, C, 5).right(D, A, C, 4)
f.dim(A, D, '6 cm', side=1, off=-14, size=8.5).dim(D, C, '4 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 {ov("AD")}{PERP}{ov("BC")}이고 {ov("AD")}=6 cm, {ov("DC")}=4 cm일 때, {tri("ABD")}의 넓이를 구하시오.',
      None, f.render(),
      ans='27 ' + CM2,
      sol=f'<p>{tri("DBA")}{SIM}{tri("DAC")} (AA 닮음)이므로 {ov("AD")}<sup>2</sup>={ov("BD")}×{ov("DC")}</p>'
          f'<p>6<sup>2</sup>={ov("BD")}×4 ∴ {ov("BD")}=9 cm</p>'
          f'<p>{tri("ABD")}={fr(1, 2)}×9×6=27({CM2})</p>'))

# 18 ------------------------------------------------------------------
B = (0.0, 0.0)
D = (12.0, 0.0)
C = (15.0, 0.0)
A = (12.0, 6.0)
M = (7.5, 0.0)
H = foot(D, A, M)
f = Fig(175, 95)
f.segs(A, B, C, close=True).seg(A, D).seg(A, M).seg(D, H).right(A, B, C, 5).right(D, A, C, 4).right(H, D, A, 4)
f.tick(B, M, 2).tick(M, C, 2)
f.dim(B, D, '12 cm', side=1, off=-12, size=8.5, curve=True).dim(D, C, '3 cm', side=1, off=-10, size=8.5, t=0.75)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-125, dist_px=11)
f.label(M, 'M', dirdeg=110, dist_px=9).label(H, 'H', dirdeg=120, dist_px=9)
add(P(f'다음 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 점 M은 {ov("BC")}의 중점이고, {ov("AD")}{PERP}{ov("BC")}, {ov("DH")}{PERP}{ov("AM")}이다. {ov("BD")}=12 cm, {ov("DC")}=3 cm일 때, {ov("AH")}의 길이를 구하시오.',
      None, f.render(), figpos='below',
      ans=f'{fr(24, 5)} cm',
      sol=f'<p>{ov("AD")}<sup>2</sup>={ov("BD")}×{ov("DC")}=12×3=36 ∴ {ov("AD")}=6 cm</p>'
          f'<p>점 M은 직각삼각형 ABC의 외심이므로 {ov("AM")}={ov("BM")}={fr(1, 2)}×15={fr(15, 2)}(cm)</p>'
          f'<p>{tri("ADM")}에서 {ov("AD")}<sup>2</sup>={ov("AH")}×{ov("AM")}이므로 36={ov("AH")}×{fr(15, 2)} ∴ {ov("AH")}={fr(24, 5)} cm</p>'))

# 19 ------------------------------------------------------------------
A, B, C, D = (0, 6), (0, 0), (8, 0), (8, 6)
Cp = reflect(C, B, D)
E = inter(B, Cp, A, D)
Fm = lerp(B, D, 0.5)
f = Fig(150, 115)
f.poly([B, Cp, D], fill='#9fd17b', stroke='#222')
f.poly([B, E, D], fill='#6bb34a', stroke='#222', opacity=0.9)
f.segs(A, B, C, D, close=True).seg(E, Fm).right(Fm, E, D, 4)
f.dim(B, C, '8 cm', side=1, off=-10, size=8.5).dim(C, D, '6 cm', side=1, off=-10, size=8.5)
f.dim(B, D, '10 cm', side=1, off=-8, size=8.5, t=0.66)
f.label(A, 'A', dirdeg=150).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=20)
f.label(Cp, "C'", dirdeg=110).label(E, 'E', dirdeg=-100, dist_px=10).label(Fm, 'F', dirdeg=-90, dist_px=9)
add(P(f'오른쪽 그림은 직사각형 ABCD를 대각선 BD를 접는 선으로 하여 접은 것이다. {ov("EF")}{PERP}{ov("BD")}이고 {ov("BC")}=8 cm, {ov("BD")}=10 cm, {ov("CD")}=6 cm일 때, {ov("EF")}의 길이를 구하시오.',
      None, f.render(),
      ans=f'{fr(15, 4)} cm',
      sol=f'<p>{ang("EBD")}={ang("DBC")} (접은 각), {ang("EDB")}={ang("DBC")} (엇각)이므로 {tri("EBD")}는 {ov("EB")}={ov("ED")}인 이등변삼각형</p>'
          f'<p>따라서 점 F는 {ov("BD")}의 중점이고 {ov("BF")}=5 cm</p>'
          f'<p>{tri("EBF")}{SIM}{tri("DBC")} (AA 닮음)이므로 {ov("EF")} : {ov("DC")}={ov("BF")} : {ov("BC")}</p>'
          f'<p>{ov("EF")} : 6=5 : 8 ∴ {ov("EF")}={fr(15, 4)} cm</p>'))

# 20 예제 1 ------------------------------------------------------------
B = (0.0, 0.0)
C = (10.0, 0.0)
A = polar(9, 60)
D = polar(10 / 3, 60)
E = (3.0, 0.0)
f = Fig(130, 105)
f.poly([B, E, D], fill='#fbf1b7', stroke='#222')
f.segs(A, B, C, close=True).seg(D, E)
f.arc(C, A, B, 10).arc(D, B, E, 9)
f.dim(B, A, '9 cm', side=1, off=12, size=8.5).dim(B, E, '3 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=150).label(E, 'E', dirdeg=-90)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ang("C")}={ang("BDE")}이고, {ov("AB")}=9 cm, {ov("BE")}=3 cm이다. {tri("ABC")}=27 {CM2}일 때, {tri("BED")}의 넓이를 구하시오.',
      None, f.render(), tag='예제',
      ans='3 ' + CM2,
      sol=f'<p>{tri("ABC")}와 {tri("EBD")}에서 {ang("C")}={ang("EDB")}, {ang("B")}는 공통이므로 {tri("ABC")}{SIM}{tri("EBD")} (AA 닮음)</p>'
          f'<p>닮음비는 {ov("AB")} : {ov("EB")}=9 : 3=3 : 1이므로 넓이의 비는 9 : 1</p>'
          f'<p>27 : {tri("BED")}=9 : 1 ∴ {tri("BED")}=3({CM2})</p>'))

# 21 유제 1 ------------------------------------------------------------
A, B, C = tri_sss(8, 12, 6)
D = lerp(A, C, 3 / 12)
f = Fig(140, 90)
f.poly([A, B, D], fill='#f9d7e2', stroke='#222')
f.segs(A, B, C, close=True).seg(B, D)
f.arc(B, A, D, 9).arc(C, A, B, 12)
f.dim(B, A, '6 cm', side=1, off=11, size=8.5).dim(A, D, '3 cm', side=1, off=9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=45)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ang("ABD")}={ang("C")}이고, {ov("AB")}=6 cm, {ov("AD")}=3 cm이다. {tri("ABD")}=8 {CM2}일 때, {tri("ABC")}의 넓이를 구하시오.',
      None, f.render(), tag='유제',
      ans='32 ' + CM2,
      sol=f'<p>{tri("ABC")}와 {tri("ADB")}에서 {ang("C")}={ang("ABD")}, {ang("A")}는 공통이므로 {tri("ABC")}{SIM}{tri("ADB")} (AA 닮음)</p>'
          f'<p>닮음비는 {ov("AB")} : {ov("AD")}=6 : 3=2 : 1이므로 넓이의 비는 4 : 1</p>'
          f'<p>{tri("ABC")} : 8=4 : 1 ∴ {tri("ABC")}=32({CM2})</p>'))

# 22 예제 2 ------------------------------------------------------------
B = (0.0, 0.0)
C = (25.0, 0.0)
A = (9.0, 12.0)
D = (9.0, 0.0)
f = Fig(160, 90)
f.segs(A, B, C, close=True).seg(A, D).right(A, B, C, 5).right(D, A, C, 5)
f.dim(A, C, '20 cm', side=1, off=-11, size=8.5).dim(B, C, '25 cm', side=1, off=-12, size=8.5, curve=True)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 {ov("AD")}{PERP}{ov("BC")}이고 {ov("AC")}=20 cm, {ov("BC")}=25 cm일 때, {ov("BD")}의 길이를 구하시오.',
      None, f.render(), tag='예제',
      ans='9 cm',
      sol=f'<p>{tri("ABC")}와 {tri("DAC")}에서 {ang("BAC")}={ang("ADC")}=90°, {ang("C")}는 공통이므로 {tri("ABC")}{SIM}{tri("DAC")} (AA 닮음)</p>'
          f'<p>{ov("AC")} : {ov("DC")}={ov("BC")} : {ov("AC")}에서 20 : {ov("DC")}=25 : 20 ∴ {ov("DC")}=16 cm</p>'
          f'<p>∴ {ov("BD")}=25−16=9(cm)</p>'))

# 23 유제 2 ------------------------------------------------------------
B = (0.0, 0.0)
C = (18.0, 0.0)
A = (2.0, math.sqrt(32))
D = (2.0, 0.0)
f = Fig(170, 80)
f.segs(A, B, C, close=True).seg(A, D).right(A, B, C, 5).right(D, A, C, 4)
f.dim(B, A, '6 cm', side=1, off=12, size=8.5).dim(B, D, '2 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-60, dist_px=10)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 {ov("AD")}{PERP}{ov("BC")}이고 {ov("AB")}=6 cm, {ov("BD")}=2 cm일 때, {ov("CD")}의 길이를 구하시오.',
      None, f.render(), figpos='below', tag='유제',
      ans='16 cm',
      sol=f'<p>{tri("ABC")}와 {tri("DBA")}에서 {ang("BAC")}={ang("BDA")}=90°, {ang("B")}는 공통이므로 {tri("ABC")}{SIM}{tri("DBA")} (AA 닮음)</p>'
          f'<p>{ov("AB")} : {ov("DB")}={ov("BC")} : {ov("BA")}에서 6 : 2={ov("BC")} : 6 ∴ {ov("BC")}=18 cm</p>'
          f'<p>∴ {ov("CD")}=18−2=16(cm)</p>'))

# 24 ------------------------------------------------------------------
f = Fig(260, 160)
dep = 2.5
p1 = cuboid(f, (0, 0), 3, 4, dep, names=['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'])
p2 = cuboid(f, (7.5, 0), 6, 8, dep * 2, names=['I', 'J', 'K', 'L', 'M', 'N', 'O', 'P'])
f.dim(p1[0], p1[3], '3 cm', side=1, off=8, size=8.5).dim(p1[3], p1[7], '4 cm', side=1, off=-12, size=8.5)
f.dim(p2[3], p2[7], '8 cm', side=1, off=-13, size=8.5)
f.text(((p1[5][0] + p1[6][0]) / 2, -1.3), '(가)', size=9).text(((p2[5][0] + p2[6][0]) / 2, -1.3), '(나)', size=9)
add(P(f'아래 그림에서 두 직육면체 (가)와 (나)는 서로 닮은 도형이고 {sq("ABFE")}에 대응하는 면이 {sq("IJNM")}일 때, 다음을 구하시오.'
      f'<div class="sub">(1) {ov("JK")}의 길이</div><div class="sub">(2) 직육면체 (가)의 부피가 30 {CM3}일 때, 직육면체 (나)의 부피</div>',
      None, f.render(), figpos='below',
      ans=f'(1) 6 cm (2) 240 {CM3}',
      sol=f'<p>(1) 닮음비는 {ov("DH")} : {ov("LP")}=4 : 8=1 : 2</p>'
          f'<p>{ov("BC")}={ov("AD")}=3 cm이고 {ov("BC")} : {ov("JK")}=1 : 2이므로 {ov("JK")}=6 cm</p>'
          f'<p>(2) 부피의 비는 1<sup>3</sup> : 2<sup>3</sup>=1 : 8이므로 (나)의 부피는 30×8=240({CM3})</p>'))

# 25 ------------------------------------------------------------------
A, B, C = tri_sss(4, 6, 8)
D = lerp(B, A, 2 / 8)
f = Fig(140, 95)
f.segs(A, B, C, close=True).seg(C, D)
f.arc(A, B, C, 12).arc(C, B, D, 12)
f.dim(C, A, '6 cm', side=1, off=-11, size=8.5).dim(B, C, '4 cm', side=1, off=-10, size=8.5).dim(D, C, '3 cm', side=1, off=9, size=8.5, t=0.5)
f.label(A, 'A', dirdeg=60).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=120)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ang("BAC")}={ang("BCD")}이고 {ov("AC")}=6 cm, {ov("BC")}=4 cm, {ov("CD")}=3 cm일 때, {ov("AD")}의 길이를 구하시오.',
      None, f.render(),
      ans='6 cm',
      sol=f'<p>{tri("ABC")}와 {tri("CBD")}에서 {ang("BAC")}={ang("BCD")}, {ang("B")}는 공통이므로 {tri("ABC")}{SIM}{tri("CBD")} (AA 닮음)</p>'
          f'<p>닮음비는 {ov("AC")} : {ov("CD")}=6 : 3=2 : 1</p>'
          f'<p>{ov("BC")} : {ov("BD")}=2 : 1에서 {ov("BD")}=2 cm, {ov("AB")} : {ov("CB")}=2 : 1에서 {ov("AB")}=8 cm</p>'
          f'<p>∴ {ov("AD")}=8−2=6(cm)</p>'))

# 26 ------------------------------------------------------------------
B = (0.0, 0.0)
C = (12.0, 0.0)
D = (7.0, 0.0)
A = (7.0, math.sqrt(75))
E = lerp(C, A, 0.6)
f = Fig(150, 110)
f.segs(A, B, C, close=True).seg(A, D).seg(B, E).right(D, A, C, 4).right(E, B, C, 4)
f.dim(A, E, '4 cm', side=1, off=12, size=8.5).dim(E, C, '6 cm', side=1, off=12, size=8.5).dim(D, C, '5 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90).label(E, 'E', dirdeg=-160, dist_px=10)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("AD")}{PERP}{ov("BC")}, {ov("AC")}{PERP}{ov("BE")}이고 {ov("AE")}=4 cm, {ov("CE")}=6 cm, {ov("CD")}=5 cm일 때, 다음을 구하시오.'
      f'<div class="sub">(1) 서로 닮은 두 삼각형 {tri("ADC")}와 {tri("BEC")}의 닮음 조건</div><div class="sub">(2) {ov("BD")}의 길이</div>',
      None, f.render(),
      ans='(1) AA 닮음 (2) 7 cm',
      sol=f'<p>(1) {tri("ADC")}와 {tri("BEC")}에서 {ang("ADC")}={ang("BEC")}=90°, {ang("C")}는 공통이므로 {tri("ADC")}{SIM}{tri("BEC")} (AA 닮음)</p>'
          f'<p>(2) {ov("CD")} : {ov("CE")}={ov("CA")} : {ov("CB")}에서 5 : 6=10 : {ov("CB")} ∴ {ov("CB")}=12 cm</p>'
          f'<p>∴ {ov("BD")}=12−5=7(cm)</p>'))

# 27 ------------------------------------------------------------------
Bm = (0.0, 0.0)
Am = (0.0, 1.5)
Cm = (2.5, 0.0)
Em = (12.5, 0.0)
Dm = (12.5, 6.0)
f = Fig(190, 110)
f.seg((-0.8, 0), (13.4, 0), w=1.4)
f.seg(Bm, Am, w=2.2, color='#2b6cb0').circle((0, 1.75), 0.25, stroke='#2b6cb0')
f.seg(Am, Cm, color='#3182ce').seg(Cm, Dm, color='#3182ce')
f.seg(Em, Dm, w=1.8).poly([Dm, (14.3, 5.6), (14.3, 4.5), (12.5, 4.9)], fill='#ffffff', stroke='#222', w=0.8)
f.poly([(2.05, 0), (2.95, 0), (2.95, 0.12), (2.05, 0.12)], fill='#9ad', stroke='#222', w=0.6)
f.right(Bm, Am, Cm, 4).right(Em, Dm, Cm, 4)
f.dim(Bm, Am, '1.5 m', side=1, off=14, size=8.5)
f.dim(Bm, Cm, '2.5 m', side=1, off=-10, size=8.5).dim(Cm, Em, '10 m', side=1, off=-10, size=8.5)
f.label(Am, 'A', dirdeg=150, dist_px=10).label(Bm, 'B', dirdeg=-120, dist_px=10).label(Cm, 'C', dirdeg=-90, dist_px=16)
f.label(Dm, 'D', dirdeg=135).label(Em, 'E', dirdeg=-45)
f.text((3.7, 0.9), '거울', size=8)
add(P(f'준호가 다음 그림과 같이 바닥에 거울을 놓고 빛의 입사각과 반사각의 크기가 같음을 이용하여 국기 게양대의 높이를 구하려고 한다. 준호의 눈높이는 1.5 m, 준호와 거울 사이의 거리는 2.5 m, 거울과 국기 게양대 사이의 거리는 10 m일 때, 국기 게양대의 높이를 구하시오. (단, 거울의 두께는 무시한다.)',
      None, f.render(), figpos='below',
      ans='6 m',
      sol=f'<p>{tri("ABC")}와 {tri("DEC")}에서 {ang("ABC")}={ang("DEC")}=90°, {ang("ACB")}={ang("DCE")}이므로 {tri("ABC")}{SIM}{tri("DEC")} (AA 닮음)</p>'
          f'<p>{ov("AB")} : {ov("DE")}={ov("BC")} : {ov("EC")}에서 1.5 : {ov("DE")}=2.5 : 10 ∴ {ov("DE")}=6(m)</p>'))

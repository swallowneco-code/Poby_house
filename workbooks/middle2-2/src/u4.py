"""4. 평행선 사이의 선분의 길이의 비"""
import math
from common import *  # noqa: F401,F403

PROBLEMS = []
add = PROBLEMS.append


def L3(f, A, B, C):
    f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
    return f


def _add_off(m, p, q, d):
    """점 m 을 선분 pq 의 왼쪽(진행방향 기준)으로 d 만큼 옮긴 점 (수학 좌표)."""
    ux, uy = q[0] - p[0], q[1] - p[1]
    l = math.hypot(ux, uy)
    return (m[0] - uy / l * d, m[1] + ux / l * d)


def centroid3(A, B, C):
    return ((A[0] + B[0] + C[0]) / 3, (A[1] + B[1] + C[1]) / 3)


# 01 ------------------------------------------------------------------
A, B, C = (5.0, 10.0), (0.0, 0.0), (13.0, 0.0)
D, E = lerp(A, B, 2 / 3), lerp(A, C, 2 / 3)
f = Fig(130, 100)
f.segs(A, B, C, close=True).seg(D, E).arrow_mark(D, E).arrow_mark(B, C)
f.dim(A, D, '8 cm', side=1, off=-12, size=8.5).dim(D, E, '6 cm', side=1, off=8, size=8.5).dim(B, C, '9 cm', side=1, off=-10, size=8.5)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("BC")}{PAR}{ov("DE")}일 때, {ov("DB")}의 길이를 구하시오.',
      None, f.render(),
      ans='4 cm',
      sol=f'<p>{ov("BC")}{PAR}{ov("DE")}이므로 {ov("AD")} : {ov("AB")}={ov("DE")} : {ov("BC")}</p>'
          f'<p>8 : {ov("AB")}=6 : 9 ∴ {ov("AB")}=12 cm</p><p>∴ {ov("DB")}=12−8=4(cm)</p>'))

# 02 ------------------------------------------------------------------
A, B, C, D = (2.5, 6.0), (0.0, 0.0), (10.0, 0.0), (12.5, 6.0)
M = (5.0, 0.0)
E = inter(B, D, A, M)
f = Fig(140, 85)
f.segs(A, B, C, D, close=True).seg(B, D).seg(A, M).tick(B, M, 2).tick(M, C, 2)
f.dim(B, D, '15 cm', side=1, off=9, size=8.5, t=0.62)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
f.label(M, 'M', dirdeg=-90, dist_px=9).label(E, 'E', dirdeg=-20, dist_px=9)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 점 M은 {ov("BC")}의 중점이고, 점 E는 대각선 BD와 {ov("AM")}의 교점이다. {ov("BD")}=15 cm일 때, {ov("BE")}의 길이는?',
      ['4 cm', f'{fr(9, 2)} cm', '5 cm', f'{fr(11, 2)} cm', '6 cm'], f.render(),
      ans='③',
      sol=f'<p>{ov("AD")}{PAR}{ov("BM")}이므로 {tri("EBM")}{SIM}{tri("EDA")} (AA 닮음)</p>'
          f'<p>{ov("BE")} : {ov("DE")}={ov("BM")} : {ov("DA")}=1 : 2</p>'
          f'<p>∴ {ov("BE")}=15×{fr(1, 3)}=5(cm)</p>'))

# 03 ------------------------------------------------------------------
B, C = (0.0, 0.0), (20.0, 0.0)
A = (7.0, 6.0)
D = (A[0] + (A[0] - B[0]) * 0.6, A[1] + (A[1] - B[1]) * 0.6)
E = (A[0] + (A[0] - C[0]) * 0.6, A[1] + (A[1] - C[1]) * 0.6)
G = (4.0, 0.0)
Fp = (4 + 7 * 0.8, 6 * 0.8)
f = Fig(150, 105)
f.segs(E, D).seg(E, C).seg(D, B).seg(B, C).seg(Fp, G)
f.arrow_mark(E, D).arrow_mark(B, C, t=0.7).arrow_mark(A, B, n=2, t=0.5).arrow_mark(Fp, G, n=2)
f.dim(B, G, sv('x'), side=1, off=-9, size=9).dim(G, C, '16', side=1, off=-9, size=8.5).dim(E, D, sv('y'), side=1, off=8, size=9)
f.label(A, 'A', dirdeg=180, dist_px=9).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
f.label(D, 'D', dirdeg=0).label(E, 'E', dirdeg=180).label(Fp, 'F', dirdeg=60).label(G, 'G', dirdeg=-90)
add(P(f'오른쪽 그림에서 {ov("BC")}{PAR}{ov("DE")}, {ov("AB")}{PAR}{ov("FG")}이고 {ov("AD")}=6, {ov("AB")}=10, {ov("FG")}=8, {ov("GC")}=16, {ov("BG")}={v("x")}, {ov("DE")}={v("y")}일 때, {v("x")}+{v("y")}의 값은?',
      ['14', '15', '16', '17', '18'], f.render(),
      ans='③',
      sol=f'<p>{tri("ABC")}에서 {ov("FG")}{PAR}{ov("AB")}이므로 {ov("CG")} : {ov("CB")}={ov("FG")} : {ov("AB")}</p>'
          f'<p>16 : (16+{v("x")})=8 : 10 ∴ {v("x")}=4</p>'
          f'<p>{ov("BC")}{PAR}{ov("DE")}이므로 {ov("AD")} : {ov("AB")}={ov("DE")} : {ov("BC")}에서 6 : 10={v("y")} : 20 ∴ {v("y")}=12</p>'
          f'<p>∴ {v("x")}+{v("y")}=16</p>'))

# 04 ------------------------------------------------------------------
A, B, C = (6.0, 12.0), (0.0, 0.0), (11.0, 4.0)
Fq = lerp(A, B, 4 / 7)
E = lerp(A, C, 4 / 7)
D = lerp(A, Fq, 4 / 7)
f = Fig(120, 125)
f.segs(A, B, C, close=True).seg(D, E).seg(Fq, E).seg(Fq, C)
f.arrow_mark(D, E).arrow_mark(Fq, C).arrow_mark(Fq, E, n=2).arrow_mark(B, C, n=2)
f.dim(A, Fq, '8 cm', side=1, off=-13, size=8.5, t=0.3)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(Fq, 'F', dirdeg=180).label(E, 'E', dirdeg=0)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("DE")}{PAR}{ov("FC")}, {ov("FE")}{PAR}{ov("BC")}이고 {ov("AD")} : {ov("DF")}=4 : 3, {ov("AF")}=8 cm일 때, {ov("BF")}의 길이를 구하시오.',
      None, f.render(),
      ans='6 cm',
      sol=f'<p>{tri("AFC")}에서 {ov("DE")}{PAR}{ov("FC")}이므로 {ov("AE")} : {ov("EC")}={ov("AD")} : {ov("DF")}=4 : 3</p>'
          f'<p>{tri("ABC")}에서 {ov("FE")}{PAR}{ov("BC")}이므로 {ov("AF")} : {ov("FB")}={ov("AE")} : {ov("EC")}=4 : 3</p>'
          f'<p>8 : {ov("FB")}=4 : 3 ∴ {ov("BF")}=6 cm</p>'))

# 05 ------------------------------------------------------------------
A, B, C = tri_sss(15, 10, 9)
D = lerp(A, B, 3 / 9)
Fq = lerp(A, C, 4 / 10)
E = lerp(B, C, 6 / 15)
f = Fig(160, 95)
f.segs(A, B, C, close=True).segs(D, E, Fq, close=True)
f.dim(A, D, '3', side=1, off=-8, size=8.5).dim(D, B, '6', side=1, off=-8, size=8.5)
f.dim(A, Fq, '4', side=1, off=8, size=8.5).dim(Fq, C, '6', side=1, off=8, size=8.5)
f.dim(B, E, '6', side=1, off=-9, size=8.5).dim(E, C, '9', side=1, off=-9, size=8.5)
L3(f, A, B, C).label(D, 'D', dirdeg=160).label(Fq, 'F', dirdeg=20).label(E, 'E', dirdeg=-90, dist_px=16)
add(P(f'오른쪽 그림의 {ov("DE")}, {ov("EF")}, {ov("FD")} 중에서 {tri("ABC")}의 어느 한 변과 평행한 선분을 말하시오.',
      None, f.render(),
      ans=f'{ov("EF")} ({ov("EF")}{PAR}{ov("AB")})',
      sol=f'<p>{ov("CE")} : {ov("EB")}=9 : 6=3 : 2, {ov("CF")} : {ov("FA")}=6 : 4=3 : 2이므로 {ov("EF")}{PAR}{ov("AB")}</p>'
          f'<p>{ov("BD")} : {ov("DA")}=6 : 3=2 : 1, {ov("BE")} : {ov("EC")}=6 : 9=2 : 3이므로 {ov("DE")}와 {ov("AC")}는 평행하지 않다.</p>'
          f'<p>{ov("AD")} : {ov("DB")}=1 : 2, {ov("AF")} : {ov("FC")}=2 : 3이므로 {ov("FD")}와 {ov("BC")}는 평행하지 않다.</p>'))

# 06 ------------------------------------------------------------------
A, B, C = tri_sss(9, 6, 8)
D = lerp(B, C, 4 / 7)
f = Fig(150, 85)
f.poly([A, B, D], fill='#e2efc9', stroke='#222')
f.segs(A, B, C, close=True).seg(A, D).arc(A, B, D, 11).arc(A, D, C, 14)
L3(f, A, B, C).label(D, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ang("A")}의 이등분선과 {ov("BC")}의 교점을 D라고 하자. {ov("AB")} : {ov("AC")}=4 : 3이고 {tri("ADC")}=18 {CM2}일 때, {tri("ABD")}의 넓이를 구하시오.',
      None, f.render(),
      ans='24 ' + CM2,
      sol=f'<p>삼각형의 내각의 이등분선의 성질에 의해 {ov("BD")} : {ov("DC")}={ov("AB")} : {ov("AC")}=4 : 3</p>'
          f'<p>{tri("ABD")}와 {tri("ADC")}는 높이가 같으므로 {tri("ABD")} : 18=4 : 3 ∴ {tri("ABD")}=24({CM2})</p>'))

# 07 ------------------------------------------------------------------
A, B, C = tri_sss(7, 6, 8)
D = (4.0, 0.0)
E = (28.0, 0.0)
Aext = lerp(B, A, 1.35)
f = Fig(200, 85)
f.segs(A, B, C, close=True).seg(C, E).seg(A, E).seg(A, D).seg(A, Aext)
f.arc(A, B, D, 10).arc(A, D, C, 13)
f.dim(B, A, '8 cm', side=1, off=10, size=8.5).dim(C, A, '6 cm', side=1, off=-9, size=8.5).dim(B, D, '4 cm', side=1, off=-9, size=8.5, t=0.4)
L3(f, A, B, C).label(D, 'D', dirdeg=-50, dist_px=10).label(E, 'E', dirdeg=-20)
add(P(f'다음 그림과 같은 {tri("ABC")}에서 {ov("AD")}는 {ang("A")}의 내각의 이등분선이고, {ov("AE")}는 {ang("A")}의 외각의 이등분선이다. {ov("AB")}=8 cm, {ov("AC")}=6 cm, {ov("BD")}=4 cm일 때, {ov("CE")}의 길이를 구하시오.',
      None, f.render(), figpos='below',
      ans='21 cm',
      sol=f'<p>{ov("BD")} : {ov("DC")}={ov("AB")} : {ov("AC")}=8 : 6=4 : 3이므로 {ov("DC")}=3 cm, {ov("BC")}=7 cm</p>'
          f'<p>외각의 이등분선의 성질에 의해 {ov("BE")} : {ov("CE")}={ov("AB")} : {ov("AC")}=4 : 3</p>'
          f'<p>(7+{ov("CE")}) : {ov("CE")}=4 : 3, 4{ov("CE")}=21+3{ov("CE")} ∴ {ov("CE")}=21 cm</p>'))

# 08 ------------------------------------------------------------------
A, B, C = (5.0, 9.0), (0.0, 0.0), (12.0, 0.0)
D, E = lerp(A, B, 0.5), lerp(A, C, 0.5)
f = Fig(120, 85)
f.segs(A, B, C, close=True).seg(D, E).tick(A, D).tick(D, B).tick(A, E, 2).tick(E, C, 2)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("AB")}, {ov("AC")}의 중점을 각각 D, E라고 할 때, 다음 중 옳지 <u>않은</u> 것은?',
      [f'{ov("DE")}{PAR}{ov("BC")}', f'{ang("ADE")}={ang("B")}', f'{ov("BC")}=2{ov("DE")}', f'{ov("AE")} : {ov("EC")}=1 : 1', f'{tri("ADE")}의 넓이는 {tri("ABC")}의 넓이의 {fr(1, 2)}이다.'],
      f.render(),
      ans='⑤',
      sol=f'<p>삼각형의 두 변의 중점을 연결한 선분의 성질에 의해 {ov("DE")}{PAR}{ov("BC")}, {ov("DE")}={fr(1, 2)}{ov("BC")}이고 {ang("ADE")}={ang("B")} (동위각)</p>'
          f'<p>⑤ {tri("ADE")}{SIM}{tri("ABC")}이고 닮음비가 1 : 2이므로 넓이의 비는 1 : 4이다. 따라서 {tri("ADE")}의 넓이는 {tri("ABC")}의 넓이의 {fr(1, 4)}이다.</p>'))

# 09 ------------------------------------------------------------------
A, B, C = (5.0, 9.0), (0.0, 0.0), (12.0, 0.0)
D, E, Fq = lerp(A, B, 0.5), lerp(B, C, 0.5), lerp(C, A, 0.5)
f = Fig(120, 85)
f.segs(A, B, C, close=True).segs(D, E, Fq, close=True)
f.tick(A, D).tick(D, B).tick(B, E, 2).tick(E, C, 2).tick(C, Fq, 3).tick(Fq, A, 3)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=-90).label(Fq, 'F', dirdeg=0)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("AB")}, {ov("BC")}, {ov("CA")}의 중점을 각각 D, E, F라고 하자. {tri("DEF")}의 둘레의 길이가 21 cm일 때, {tri("ABC")}의 둘레의 길이를 구하시오.',
      None, f.render(),
      ans='42 cm',
      sol=f'<p>{ov("DE")}={fr(1, 2)}{ov("AC")}, {ov("EF")}={fr(1, 2)}{ov("AB")}, {ov("FD")}={fr(1, 2)}{ov("BC")}이므로</p>'
          f'<p>({tri("ABC")}의 둘레)=2×({tri("DEF")}의 둘레)=2×21=42(cm)</p>'))

# 10 ------------------------------------------------------------------
B, C, A = (0.0, 0.0), (24.0, 0.0), (24.0, 16.0)
D = (12.0, 8.0)
Fq = (12.0, 0.0)
E = (18.0, 4.0)
G = (18.0, 0.0)
f = Fig(150, 100)
f.segs(A, B, C, close=True).seg(D, C).seg(D, Fq).seg(E, G)
f.right(C, A, B, 5).right(Fq, D, C, 4).right(G, E, C, 4)
f.tick(A, D).tick(D, B).tick(D, E, 2).tick(E, C, 2)
f.dim(D, Fq, '8', side=1, off=-8, size=8.5).dim(E, G, sv('y'), side=1, off=-7, size=9).dim(A, C, sv('x'), side=1, off=-9, size=9)
L3(f, A, B, C).label(D, 'D', dirdeg=110).label(E, 'E', dirdeg=60, dist_px=7).label(Fq, 'F', dirdeg=-90).label(G, 'G', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("C")}=90°인 직각삼각형 ABC에서 두 점 D, E는 각각 {ov("AB")}, {ov("DC")}의 중점이다. 두 점 D, E에서 {ov("BC")}에 내린 수선의 발을 각각 F, G라 하고 {ov("DF")}=8, {ov("AC")}={v("x")}, {ov("EG")}={v("y")}일 때, {v("x")}−{v("y")}의 값은?',
      ['8', '10', '12', '14', '16'], f.render(),
      ans='③',
      sol=f'<p>{ov("DF")}{PAR}{ov("AC")}이고 점 D가 {ov("AB")}의 중점이므로 점 F는 {ov("BC")}의 중점이고 {ov("AC")}=2{ov("DF")}=16 ∴ {v("x")}=16</p>'
          f'<p>{tri("DFC")}에서 {ov("EG")}{PAR}{ov("DF")}이고 점 E가 {ov("DC")}의 중점이므로 {ov("EG")}={fr(1, 2)}{ov("DF")}=4 ∴ {v("y")}=4</p>'
          f'<p>∴ {v("x")}−{v("y")}=12</p>'))

# 11 ------------------------------------------------------------------
A, B, C = (3.0, 9.0), (0.0, 0.0), (11.0, 0.0)
D = lerp(B, C, 0.5)
E, Fq = lerp(A, C, 1 / 3), lerp(A, C, 2 / 3)
G = inter(B, E, A, D)
f = Fig(125, 105)
f.segs(A, B, C, close=True).seg(B, E).seg(A, D).seg(D, Fq)
f.tick(B, D, 2).tick(D, C, 2).tick(A, E).tick(E, Fq).tick(Fq, C)
f.dim(B, G, '18 cm', side=1, off=10, size=8.5)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(E, 'E', dirdeg=20).label(Fq, 'F', dirdeg=20).label(G, 'G', dirdeg=-20, dist_px=9)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("BC")}의 중점을 D, {ov("AC")}의 삼등분점을 각각 E, F라고 하자. {ov("BE")}와 {ov("AD")}의 교점을 G라 하고 {ov("BG")}=18 cm일 때, {ov("GE")}의 길이는?',
      ['4 cm', '5 cm', '6 cm', '7 cm', '8 cm'], f.render(),
      ans='③',
      sol=f'<p>{tri("CEB")}에서 점 D, F는 각각 {ov("CB")}, {ov("CE")}의 중점이므로 {ov("DF")}{PAR}{ov("BE")}, {ov("DF")}={fr(1, 2)}{ov("BE")}</p>'
          f'<p>{tri("ADF")}에서 점 E는 {ov("AF")}의 중점이고 {ov("GE")}{PAR}{ov("DF")}이므로 {ov("GE")}={fr(1, 2)}{ov("DF")}={fr(1, 4)}{ov("BE")}</p>'
          f'<p>{ov("BG")}={fr(3, 4)}{ov("BE")}=18 ∴ {ov("BE")}=24 cm, {ov("GE")}=6 cm</p>'))

# 12 ------------------------------------------------------------------
B, C, A = (0.0, 0.0), (16.0, 0.0), (7.0, 12.0)
D = lerp(A, C, 0.5)
E = (-8.0, 0.0)
Fq = inter(E, D, A, B)
f = Fig(150, 100)
f.segs(A, B, C, close=True).seg(E, B).seg(E, D)
f.tick(A, D).tick(D, C).tick(E, Fq, 2).tick(Fq, D, 2)
f.dim(E, C, '24 cm', side=1, off=-11, size=8.5, curve=True)
L3(f, A, B, C).label(D, 'D', dirdeg=20).label(E, 'E', dirdeg=180).label(Fq, 'F', dirdeg=135, dist_px=9)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 점 D는 {ov("AC")}의 중점이고, 점 F는 {ov("AB")} 위의 점이다. {ov("BC")}의 연장선 위의 점 E에 대하여 {ov("DF")}={ov("EF")}이고 {ov("EC")}=24 cm일 때, {ov("BC")}의 길이를 구하시오.',
      None, f.render(),
      ans='16 cm',
      sol=f'<p>점 D를 지나고 {ov("BC")}에 평행한 직선이 {ov("AB")}와 만나는 점을 G라고 하면 점 G는 {ov("AB")}의 중점이므로 {ov("GD")}={fr(1, 2)}{ov("BC")}</p>'
          f'<p>{tri("FEB")}와 {tri("FDG")}에서 {ov("FE")}={ov("FD")}, {ang("EFB")}={ang("DFG")}, {ang("FEB")}={ang("FDG")} (엇각)이므로 {tri("FEB")}{CONG}{tri("FDG")} (ASA 합동) ∴ {ov("EB")}={ov("GD")}={fr(1, 2)}{ov("BC")}</p>'
          f'<p>{ov("EC")}={fr(1, 2)}{ov("BC")}+{ov("BC")}={fr(3, 2)}{ov("BC")}=24 ∴ {ov("BC")}=16 cm</p>'))

# 13 ------------------------------------------------------------------
A, B, C, D = (0, 5), (0, 0), (12, 0), (12, 5)
E, Fq, G, H = (0, 2.5), (6, 0), (12, 2.5), (6, 5)
f = Fig(150, 80)
f.segs(A, B, C, D, close=True).segs(E, Fq, G, H, close=True).seg(B, D, dash=True)
f.tick(A, E).tick(E, B).tick(D, G).tick(G, C).tick(A, H, 2).tick(H, D, 2).tick(B, Fq, 2).tick(Fq, C, 2)
f.dim(B, D, '13 cm', side=1, off=8, size=8.5, t=0.35)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
f.label(E, 'E', dirdeg=180).label(Fq, 'F', dirdeg=-90).label(G, 'G', dirdeg=0).label(H, 'H', dirdeg=90)
add(P(f'오른쪽 그림과 같은 직사각형 ABCD에서 {ov("AB")}, {ov("BC")}, {ov("CD")}, {ov("DA")}의 중점을 각각 E, F, G, H라고 하자. {ov("BD")}=13 cm일 때, {sq("EFGH")}의 둘레의 길이를 구하시오.',
      None, f.render(),
      ans='26 cm',
      sol=f'<p>{ov("EH")}={ov("FG")}={fr(1, 2)}{ov("BD")}={fr(13, 2)} cm</p>'
          f'<p>직사각형의 두 대각선의 길이는 같으므로 {ov("EF")}={ov("HG")}={fr(1, 2)}{ov("AC")}={fr(1, 2)}{ov("BD")}={fr(13, 2)} cm</p>'
          f'<p>(둘레의 길이)=4×{fr(13, 2)}=26(cm)</p>'))

# 14 ------------------------------------------------------------------
A, D, B, C = (3.0, 6.0), (9.0, 6.0), (0.0, 0.0), (12.0, 0.0)
M, N = (1.5, 3.0), (10.5, 3.0)
E = inter(M, N, B, D)
Fq = inter(M, N, A, C)
f = Fig(140, 85)
f.segs(A, B, C, D, close=True).seg(M, N).seg(A, C).seg(B, D)
f.tick(A, M).tick(M, B).tick(D, N, 2).tick(N, C, 2)
f.arrow_mark(A, D).arrow_mark(B, C)
f.dim(A, D, '6 cm', side=1, off=8, size=8.5)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(M, 'M', dirdeg=180).label(N, 'N', dirdeg=0).label(E, 'E', dirdeg=-100, dist_px=9).label(Fq, 'F', dirdeg=-80, dist_px=9)
add(P(f'오른쪽 그림과 같이 {ov("AD")}{PAR}{ov("BC")}인 사다리꼴 ABCD에서 두 점 M, N은 각각 {ov("AB")}, {ov("DC")}의 중점이고, {ov("MN")}과 {ov("BD")}, {ov("AC")}의 교점을 각각 E, F라고 하자. {ov("ME")}={ov("EF")}={ov("FN")}일 때, {ov("BC")}의 길이는?',
      ['10 cm', '11 cm', '12 cm', '13 cm', '14 cm'], f.render(),
      ans='③',
      sol=f'<p>{tri("BDA")}에서 {ov("ME")}={fr(1, 2)}{ov("AD")}=3 cm이므로 {ov("EF")}={ov("FN")}=3 cm</p>'
          f'<p>{tri("ABC")}에서 {ov("MF")}={fr(1, 2)}{ov("BC")}이고 {ov("MF")}=3+3=6(cm)</p><p>∴ {ov("BC")}=12 cm</p>'))

# 15 ------------------------------------------------------------------
yl, ym, yn = 14.0, 6.0, 0.0
f = Fig(150, 100)
for y, nm in ((yl, 'l'), (ym, 'm'), (yn, 'n')):
    f.seg((-1, y), (17, y)).text((17.8, y), nm, size=10, italic=True)
t1a, t1b = (1.5, 16.0), (3.5, -1.0)
P1 = inter(t1a, t1b, (0, yl), (1, yl))
P2 = inter(t1a, t1b, (0, ym), (1, ym))
P3 = inter(t1a, t1b, (0, yn), (1, yn))
t2a, t2b = (4.0, 16.4), (15.0, -1.5)
Q1 = inter(t2a, t2b, (0, yl), (1, yl))
Q2 = inter(t2a, t2b, (0, ym), (1, ym))
Q3 = inter(t2a, t2b, (0, yn), (1, yn))
f.seg(t1a, t1b).seg(t2a, t2b)
f.dim(P1, P2, '8', side=1, off=-8, size=9).dim(P2, P3, '6', side=1, off=-8, size=9)
f.dim(Q1, Q2, '12', side=1, off=10, size=9).dim(Q2, Q3, sv('x'), side=1, off=9, size=10)
add(P(f'오른쪽 그림에서 {v("l")}{PAR}{v("m")}{PAR}{v("n")}일 때, {v("x")}의 값을 구하시오.',
      None, f.render(),
      ans='9',
      sol=f'<p>평행선 사이의 선분의 길이의 비에 의해 8 : 6=12 : {v("x")}</p><p>8{v("x")}=72 ∴ {v("x")}=9</p>'))

# 16 ------------------------------------------------------------------
A, D, B, C = (4.5, 9.0), (16.5, 9.0), (0.0, 0.0), (21.0, 0.0)
M, N = lerp(A, B, 2 / 3), lerp(D, C, 2 / 3)
f = Fig(140, 85)
f.segs(A, B, C, D, close=True).seg(M, N)
f.arrow_mark(A, D).arrow_mark(M, N).arrow_mark(B, C)
f.dim(A, D, '12', side=1, off=8, size=8.5).dim(B, C, '21', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(M, 'M', dirdeg=180).label(N, 'N', dirdeg=0)
add(P(f'오른쪽 그림과 같은 사다리꼴 ABCD에서 {ov("AD")}{PAR}{ov("MN")}{PAR}{ov("BC")}이고, {ov("AM")} : {ov("MB")}=2 : 1이다. {ov("AD")}=12, {ov("BC")}=21일 때, {ov("MN")}의 길이를 구하시오.',
      None, f.render(),
      ans='18',
      sol=f'<p>점 A를 지나고 {ov("DC")}에 평행한 직선이 {ov("MN")}, {ov("BC")}와 만나는 점을 각각 P, Q라고 하면</p>'
          f'<p>{ov("PN")}={ov("QC")}={ov("AD")}=12, {ov("BQ")}=21−12=9</p>'
          f'<p>{tri("ABQ")}에서 {ov("MP")} : {ov("BQ")}={ov("AM")} : {ov("AB")}=2 : 3이므로 {ov("MP")}=6</p>'
          f'<p>∴ {ov("MN")}=6+12=18</p>'))

# 17 ------------------------------------------------------------------
A, D, B, C = (2.0, 7.0), (14.0, 7.0), (0.0, 0.0), (16.0, 0.0)
E, Fq = lerp(A, B, 3 / 7), lerp(D, C, 3 / 7)
O = inter(A, C, B, D)
f = Fig(130, 90)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D).seg(E, Fq)
f.arrow_mark(A, D).arrow_mark(E, Fq, t=0.75).arrow_mark(B, C)
f.dim(A, D, '12', side=1, off=8, size=8.5).dim(A, E, '6', side=1, off=9, size=8.5).dim(E, B, '8', side=1, off=9, size=8.5)
f.dim(B, C, sv('x'), side=1, off=-9, size=9)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(E, 'E', dirdeg=180).label(Fq, 'F', dirdeg=0).label(O, 'O', dirdeg=90, dist_px=9)
add(P(f'오른쪽 그림과 같은 사다리꼴 ABCD에서 {ov("AD")}{PAR}{ov("EF")}{PAR}{ov("BC")}이고 두 대각선의 교점 O는 {ov("EF")} 위의 점이다. {ov("AE")}=6, {ov("EB")}=8, {ov("AD")}=12, {ov("BC")}={v("x")}일 때, {v("x")}의 값은?',
      ['14', '15', '16', '17', '18'], f.render(),
      ans='③',
      sol=f'<p>{tri("ABC")}에서 {ov("EO")}{PAR}{ov("BC")}이므로 {ov("AO")} : {ov("OC")}={ov("AE")} : {ov("EB")}=6 : 8=3 : 4</p>'
          f'<p>{ov("AD")}{PAR}{ov("BC")}이므로 {tri("OAD")}{SIM}{tri("OCB")}이고 {ov("AD")} : {ov("CB")}={ov("AO")} : {ov("CO")}=3 : 4</p>'
          f'<p>12 : {v("x")}=3 : 4 ∴ {v("x")}=16</p>'))

# 18 ------------------------------------------------------------------
B, C, A, D = (0.0, 0.0), (15.0, 0.0), (0.0, 12.0), (15.0, 6.0)
E = inter(A, C, B, D)
Fq = (E[0], 0.0)
f = Fig(150, 105)
f.poly([E, B, C], fill='#f9d7e2', stroke='#222')
f.segs(A, B, C, D).seg(A, C).seg(B, D).seg(E, Fq)
f.right(B, A, C, 5).right(C, B, D, 5).right(Fq, E, C, 4)
f.dim(B, A, '12 cm', side=1, off=14, size=8.5).dim(C, D, '6 cm', side=1, off=-14, size=8.5)
f.dim(B, C, '15 cm', side=1, off=-12, size=8.5, curve=True)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(E, 'E', dirdeg=90, dist_px=9).label(Fq, 'F', dirdeg=-90)
add(P(f'다음 그림에서 {ang("ABC")}={ang("EFC")}={ang("DCB")}=90°일 때, {tri("EBC")}의 넓이를 구하시오.',
      None, f.render(), figpos='below',
      ans='30 ' + CM2,
      sol=f'<p>{ov("AB")}{PAR}{ov("DC")}이므로 {tri("EAB")}{SIM}{tri("ECD")}이고 {ov("BE")} : {ov("ED")}={ov("AB")} : {ov("CD")}=12 : 6=2 : 1</p>'
          f'<p>{tri("BCD")}에서 {ov("EF")}{PAR}{ov("DC")}이므로 {ov("EF")} : {ov("DC")}={ov("BE")} : {ov("BD")}=2 : 3</p>'
          f'<p>{ov("EF")} : 6=2 : 3 ∴ {ov("EF")}=4 cm</p>'
          f'<p>{tri("EBC")}={fr(1, 2)}×15×4=30({CM2})</p>'))

# 19 ------------------------------------------------------------------
A, B, C = (5.0, 12.0), (0.0, 0.0), (12.0, 0.0)
D = lerp(B, C, 0.5)
G = centroid3(A, B, C)
G2 = centroid3(G, B, C)
f = Fig(120, 120)
f.segs(A, B, C, close=True).seg(A, D).seg(B, G).seg(C, G).dot(G, 1.4).dot(G2, 1.4)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
f.label(G, 'G', dirdeg=0, dist_px=8).label(G2, "G'", dirdeg=0, dist_px=9)
f.dim(G2, D, '2 cm', side=1, off=-14, size=8.5)
add(P(f'오른쪽 그림에서 두 점 G, G′은 각각 {tri("ABC")}, {tri("GBC")}의 무게중심이다. {ov("G′D")}=2 cm일 때, {ov("AD")}의 길이를 구하시오.',
      None, f.render(),
      ans='18 cm',
      sol=f'<p>점 G′은 {tri("GBC")}의 무게중심이므로 {ov("GD")}=3{ov("G′D")}=3×2=6(cm)</p>'
          f'<p>점 G는 {tri("ABC")}의 무게중심이므로 {ov("AD")}=3{ov("GD")}=3×6=18(cm)</p>'))

# 20 ------------------------------------------------------------------
A, B, C = (4.0, 10.0), (0.0, 0.0), (12.0, 0.0)
Fm = lerp(B, C, 0.5)
G = centroid3(A, B, C)
D = inter(G, (G[0] + 1, G[1]), A, B)
E = inter(G, (G[0] + 1, G[1]), A, C)
f = Fig(125, 105)
f.segs(A, B, C, close=True).seg(A, Fm).seg(D, E).dot(G, 1.4)
f.arrow_mark(D, E, t=0.25).arrow_mark(B, C, t=0.25)
f.dim(G, E, '6 cm', side=1, off=-8, size=8.5)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0).label(Fm, 'F', dirdeg=-90).label(G, 'G', dirdeg=120, dist_px=8)
add(P(f'오른쪽 그림에서 점 G는 {tri("ABC")}의 무게중심이다. {ov("DE")}{PAR}{ov("BC")}이고 {ov("GE")}=6 cm일 때, {ov("BC")}의 길이를 구하시오.',
      None, f.render(),
      ans='18 cm',
      sol=f'<p>{ov("AF")}는 중선이고 {ov("AG")} : {ov("AF")}=2 : 3</p>'
          f'<p>{tri("AFC")}에서 {ov("GE")}{PAR}{ov("FC")}이므로 {ov("GE")} : {ov("FC")}=2 : 3, 6 : {ov("FC")}=2 : 3 ∴ {ov("FC")}=9 cm</p>'
          f'<p>점 F는 {ov("BC")}의 중점이므로 {ov("BC")}=2×9=18(cm)</p>'))

# 21 ------------------------------------------------------------------
A, B, C = (5.0, 12.0), (0.0, 0.0), (12.0, 0.0)
D = lerp(B, C, 0.5)
E = lerp(A, C, 0.5)
G = centroid3(A, B, C)
Fq = lerp(A, D, 0.5)
f = Fig(120, 120)
f.segs(A, B, C, close=True).seg(A, D).seg(B, E).seg(Fq, E).dot(G, 1.4)
f.arrow_mark(Fq, E).arrow_mark(B, C, t=0.3)
f.dim(A, D, '18 cm', side=1, off=14, size=8.5, t=0.3)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(E, 'E', dirdeg=0).label(Fq, 'F', dirdeg=150, dist_px=9).label(G, 'G', dirdeg=-30, dist_px=9)
add(P(f'오른쪽 그림에서 점 G는 {tri("ABC")}의 무게중심이고, {ov("FE")}{PAR}{ov("BC")}이다. {ov("AD")}=18 cm일 때, {ov("FG")}의 길이는?',
      ['2 cm', f'{fr(5, 2)} cm', '3 cm', f'{fr(7, 2)} cm', '4 cm'], f.render(),
      ans='③',
      sol=f'<p>점 G가 무게중심이므로 {ov("BE")}는 중선이고 점 E는 {ov("AC")}의 중점이다.</p>'
          f'<p>{tri("ADC")}에서 {ov("FE")}{PAR}{ov("DC")}이므로 점 F는 {ov("AD")}의 중점 → {ov("AF")}=9 cm</p>'
          f'<p>{ov("AG")}={fr(2, 3)}{ov("AD")}=12 cm ∴ {ov("FG")}=12−9=3(cm)</p>'))

# 22 ------------------------------------------------------------------
C, B, A = (6.0, 0.0), (0.0, 0.0), (6.0, 10.0)
D = lerp(B, C, 0.5)
G = centroid3(A, B, C)
f = Fig(100, 125)
f.poly([G, D, C], fill='#fbe3b0', stroke='#222')
f.segs(A, B, C, close=True).seg(A, D).seg(G, C).dot(G, 1.4).right(C, A, B, 5)
f.dim(A, C, '10 cm', side=1, off=15, size=8.5).dim(B, C, '6 cm', side=1, off=-12, size=8.5, curve=True)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(G, 'G', dirdeg=160, dist_px=8)
add(P(f'오른쪽 그림에서 점 G는 {ang("C")}=90°인 직각삼각형 ABC의 무게중심이다. {ov("AC")}=10 cm, {ov("BC")}=6 cm일 때, {tri("GDC")}의 넓이를 구하시오.',
      None, f.render(),
      ans='5 ' + CM2,
      sol=f'<p>{tri("ABC")}={fr(1, 2)}×6×10=30({CM2})</p>'
          f'<p>점 G가 무게중심이므로 {tri("GDC")}={fr(1, 6)}{tri("ABC")}={fr(1, 6)}×30=5({CM2})</p>'))

# 23 ------------------------------------------------------------------
A, B, C = (5.0, 12.0), (0.0, 0.0), (12.0, 0.0)
D = lerp(B, C, 0.5)
G = centroid3(A, B, C)
G2 = centroid3(G, B, C)
f = Fig(120, 120)
f.poly([A, B, C], fill='#e3e6fb', stroke='#222')
f.poly([G, B, G2], fill='#b8bff0', stroke='#222')
f.seg(A, D).seg(B, G).seg(C, G).seg(B, G2).seg(C, G2).dot(G, 1.4).dot(G2, 1.4)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
f.label(G, 'G', dirdeg=20, dist_px=8).label(G2, "G'", dirdeg=10, dist_px=10)
add(P(f'오른쪽 그림에서 두 점 G, G′은 각각 {tri("ABC")}, {tri("GBC")}의 무게중심이다. {tri("GBG′")}=3 {CM2}일 때, {tri("ABC")}의 넓이를 구하시오.',
      None, f.render(),
      ans='27 ' + CM2,
      sol=f'<p>점 G′은 {tri("GBC")}의 무게중심이므로 {tri("GBC")}=3{tri("GBG′")}=9({CM2})</p>'
          f'<p>점 G는 {tri("ABC")}의 무게중심이므로 {tri("ABC")}=3{tri("GBC")}=27({CM2})</p>'))

# 24 ------------------------------------------------------------------
def _pg():
    A, B, C, D = (2.5, 6.0), (0.0, 0.0), (10.0, 0.0), (12.5, 6.0)
    M, N = lerp(B, C, 0.5), lerp(C, D, 0.5)
    Pp, Q = inter(B, D, A, M), inter(B, D, A, N)
    return A, B, C, D, M, N, Pp, Q

A, B, C, D, M, N, Pp, Q = _pg()
f = Fig(150, 90)
f.segs(A, B, C, D, close=True).seg(B, D).seg(A, M).seg(A, N).seg(M, N)
f.tick(B, M, 2).tick(M, C, 2).tick(C, N).tick(N, D)
f.dim(B, Pp, '6 cm', side=1, off=11, size=8.5, t=0.4)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
f.label(M, 'M', dirdeg=-90).label(N, 'N', dirdeg=0).label(Pp, 'P', dirdeg=-60, dist_px=9).label(Q, 'Q', dirdeg=100, dist_px=9)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ov("BC")}, {ov("CD")}의 중점을 각각 M, N이라 하고, {ov("BD")}와 {ov("AM")}, {ov("AN")}의 교점을 각각 P, Q라고 하자. {ov("BP")}=6 cm일 때, {ov("MN")}의 길이를 구하시오.',
      None, f.render(),
      ans='9 cm',
      sol=f'<p>대각선 AC를 그어 BD와의 교점을 O라고 하면 점 P는 {tri("ABC")}의 무게중심이다.</p>'
          f'<p>{ov("BP")}={fr(2, 3)}{ov("BO")}={fr(1, 3)}{ov("BD")}이므로 {ov("BD")}=3×6=18(cm)</p>'
          f'<p>{tri("CDB")}에서 {ov("MN")}={fr(1, 2)}{ov("BD")}=9 cm</p>'))

# 25 ------------------------------------------------------------------
A, B, C, D, M, N, Pp, Q = _pg()
O = lerp(A, C, 0.5)
f = Fig(150, 90)
f.poly([Pp, M, C, N, Q], fill='#cfe8c0', stroke=None)
f.segs(A, B, C, D, close=True).seg(B, D).seg(A, C).seg(A, M).seg(A, N).seg(M, N)
f.tick(B, M, 2).tick(M, C, 2).tick(C, N).tick(N, D)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
f.label(M, 'M', dirdeg=-90).label(N, 'N', dirdeg=0).label(Pp, 'P', dirdeg=160, dist_px=9).label(Q, 'Q', dirdeg=100, dist_px=9).label(O, 'O', dirdeg=90, dist_px=8)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ov("BC")}, {ov("CD")}의 중점을 각각 M, N이라 하고, {ov("BD")}와 {ov("AM")}, {ov("AN")}의 교점을 각각 P, Q라고 하자. {sq("ABCD")}=60 {CM2}일 때, 색칠한 오각형 PMCNQ의 넓이를 구하시오. (단, 점 O는 두 대각선의 교점이다.)',
      None, f.render(),
      ans='20 ' + CM2,
      sol=f'<p>점 P, Q는 각각 {tri("ABC")}, {tri("ACD")}의 무게중심이다.</p>'
          f'<p>{tri("ABC")}={tri("ACD")}=30 {CM2}이므로</p>'
          f'<p>{sq("POCM")}={fr(2, 6)}{tri("ABC")}=10({CM2}), {sq("QOCN")}={fr(2, 6)}{tri("ACD")}=10({CM2})</p>'
          f'<p>(색칠한 부분의 넓이)=10+10=20({CM2})</p>'))

# 26 예제 1 ------------------------------------------------------------
B, C = (0.0, 0.0), (10.0, 0.0)
A = polar(15, 70)
D = polar(6, 70)
Fq = (6.0, 0.0)
E = (D[0] + 6, D[1])
f = Fig(110, 140)
f.segs(A, B, C, close=True).segs(D, E, Fq)
f.tick(B, D).tick(D, E).tick(E, Fq).tick(Fq, B)
f.dim(B, A, '15 cm', side=1, off=14, size=8.5, curve=True).dim(B, C, '10 cm', side=1, off=-12, size=8.5, curve=True)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0).label(Fq, 'F', dirdeg=-90)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {sq("DBFE")}는 마름모이다. {ov("AB")}=15 cm, {ov("BC")}=10 cm일 때, {sq("DBFE")}의 둘레의 길이를 구하시오.',
      None, f.render(), tag='예제',
      ans='24 cm',
      sol=f'<p>마름모 DBFE의 한 변의 길이를 {v("x")} cm라고 하면 {ov("DE")}{PAR}{ov("BC")}이므로</p>'
          f'<p>{ov("AD")} : {ov("AB")}={ov("DE")} : {ov("BC")}에서 (15−{v("x")}) : 15={v("x")} : 10</p>'
          f'<p>15{v("x")}=150−10{v("x")}, 25{v("x")}=150 ∴ {v("x")}=6</p>'
          f'<p>(둘레의 길이)=4×6=24(cm)</p>'))

# 27 유제 1 ------------------------------------------------------------
B, C = (0.0, 0.0), (12.0, 0.0)
A = polar(8, 110, C)
s = 24 / 5
E = polar(s, 110, C)
Fq = (12 - s, 0.0)
D = (Fq[0] + E[0] - C[0], E[1])
f = Fig(150, 100)
f.segs(A, B, C, close=True).segs(D, Fq).seg(D, E)
f.tick(D, E).tick(E, C).tick(C, Fq).tick(Fq, D)
f.dim(C, A, '8 cm', side=1, off=-13, size=8.5, t=0.8).dim(B, C, '12 cm', side=1, off=-12, size=8.5, curve=True)
L3(f, A, B, C).label(D, 'D', dirdeg=120).label(E, 'E', dirdeg=20).label(Fq, 'F', dirdeg=-90)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {sq("DFCE")}는 마름모이다. {ov("AC")}=8 cm, {ov("BC")}=12 cm일 때, {sq("DFCE")}의 둘레의 길이를 구하시오.',
      None, f.render(), tag='유제',
      ans=f'{fr(96, 5)} cm',
      sol=f'<p>마름모 DFCE의 한 변의 길이를 {v("x")} cm라고 하면 {ov("DE")}{PAR}{ov("BC")}이므로</p>'
          f'<p>{ov("AE")} : {ov("AC")}={ov("DE")} : {ov("BC")}에서 (8−{v("x")}) : 8={v("x")} : 12</p>'
          f'<p>8{v("x")}=96−12{v("x")}, 20{v("x")}=96 ∴ {v("x")}={fr(24, 5)}</p>'
          f'<p>(둘레의 길이)=4×{fr(24, 5)}={fr(96, 5)}(cm)</p>'))

# 28 예제 2 ------------------------------------------------------------
B, C, A = (0.0, 0.0), (12.0, 0.0), (12.0, 16.0)
D = lerp(A, B, 0.5)
G = centroid3(A, B, C)
f = Fig(110, 140)
f.segs(A, B, C, close=True).seg(C, D).dot(G, 1.4).right(C, A, B, 5)
f.dim(B, C, '12 cm', side=1, off=-12, size=8.5, curve=True)
f.frac(_add_off(lerp(D, G, 0.35), D, G, -2.8), '10', '3', 'cm', size=8)
L3(f, A, B, C).label(D, 'D', dirdeg=135).label(G, 'G', dirdeg=-10, dist_px=8)
add(P(f'오른쪽 그림에서 점 G는 {ang("C")}=90°인 직각삼각형 ABC의 무게중심이다. {ov("BC")}=12 cm, {ov("DG")}={fr(10, 3)} cm일 때, {ov("AC")}의 길이를 구하시오.',
      None, f.render(), tag='예제',
      ans='16 cm',
      sol=f'<p>점 G는 {tri("ABC")}의 무게중심이므로 {ov("CD")}=3{ov("DG")}=3×{fr(10, 3)}=10(cm)</p>'
          f'<p>점 D는 직각삼각형 ABC의 외심이므로 {ov("AD")}={ov("BD")}={ov("CD")}=10 cm ∴ {ov("AB")}=20 cm</p>'
          f'<p>{ov("AC")}<sup>2</sup>=20<sup>2</sup>−12<sup>2</sup>=256 ∴ {ov("AC")}=16 cm</p>'))

# 29 유제 2 ------------------------------------------------------------
B, C = (0.0, 0.0), (26.0, 0.0)
A = (24 * 24 / 26, 24 * 10 / 26)
D = (13.0, 0.0)
G = centroid3(A, B, C)
f = Fig(170, 85)
f.segs(A, B, C, close=True).seg(A, D).dot(G, 1.4).right(A, B, C, 5)
f.dim(A, C, '10 cm', side=1, off=-12, size=8.5)
f.frac(_add_off(lerp(A, G, 0.5), A, G, -2.2), '26', '3', 'cm', size=8)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(G, 'G', dirdeg=180, dist_px=8)
add(P(f'오른쪽 그림에서 점 G는 {ang("A")}=90°인 직각삼각형 ABC의 무게중심이다. {ov("AC")}=10 cm, {ov("AG")}={fr(26, 3)} cm일 때, {ov("AB")}의 길이를 구하시오.',
      None, f.render(), figpos='below', tag='유제',
      ans='24 cm',
      sol=f'<p>점 G는 {tri("ABC")}의 무게중심이므로 {ov("AD")}={fr(3, 2)}{ov("AG")}={fr(3, 2)}×{fr(26, 3)}=13(cm)</p>'
          f'<p>점 D는 직각삼각형 ABC의 외심이므로 {ov("BD")}={ov("CD")}={ov("AD")}=13 cm ∴ {ov("BC")}=26 cm</p>'
          f'<p>{ov("AB")}<sup>2</sup>=26<sup>2</sup>−10<sup>2</sup>=576 ∴ {ov("AB")}=24 cm</p>'))

# 30 ------------------------------------------------------------------
B, C, Q = (0.0, 0.0), (15.0, 0.0), (6.0, 0.0)
A = (5.0, 13.0)
D, E, Pp = lerp(A, B, 2 / 3), lerp(A, C, 2 / 3), lerp(A, Q, 2 / 3)
f = Fig(120, 125)
f.segs(A, B, C, close=True).seg(D, E).seg(A, Q)
f.arrow_mark(D, E, t=0.75).arrow_mark(B, C, t=0.8)
f.dim(A, D, sv('x'), side=1, off=10, size=9).dim(D, B, '3', side=1, off=9, size=8.5)
f.dim(D, Pp, '4', side=1, off=7, size=8.5).dim(Pp, E, '6', side=1, off=7, size=8.5)
f.dim(B, Q, '6', side=1, off=-9, size=8.5).dim(Q, C, sv('y'), side=1, off=-9, size=9)
L3(f, A, B, C).label(D, 'D', dirdeg=180).label(E, 'E', dirdeg=0).label(Pp, 'P', dirdeg=-30, dist_px=9).label(Q, 'Q', dirdeg=-90, dist_px=16)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("DE")}{PAR}{ov("BC")}일 때, {v("x")}+{v("y")}의 값을 구하시오.',
      None, f.render(),
      ans='15',
      sol=f'<p>{tri("ABQ")}에서 {ov("DP")}{PAR}{ov("BQ")}이므로 {ov("AD")} : {ov("AB")}={ov("DP")} : {ov("BQ")}</p>'
          f'<p>{v("x")} : ({v("x")}+3)=4 : 6, 6{v("x")}=4{v("x")}+12 ∴ {v("x")}=6</p>'
          f'<p>{ov("DP")} : {ov("BQ")}={ov("AP")} : {ov("AQ")}={ov("PE")} : {ov("QC")}이므로 4 : 6=6 : {v("y")} ∴ {v("y")}=9</p>'
          f'<p>∴ {v("x")}+{v("y")}=15</p>'))

# 31 ------------------------------------------------------------------
A, B, C = tri_sss(15, 10, 15)
D = (9.0, 0.0)
E = lerp(B, A, 25 / 15)
f = Fig(140, 125)
f.segs(A, B, C, close=True).seg(A, D).seg(A, E).seg(C, E)
f.arc(A, B, D, 10).arc(A, D, C, 13)
f.arrow_mark(A, D, t=0.45).arrow_mark(C, E, t=0.45)
f.dim(B, A, '15 cm', side=1, off=12, size=8.5).dim(A, C, '10 cm', side=1, off=-12, size=8.5, t=0.62).dim(B, D, '9 cm', side=1, off=-9, size=8.5)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(E, 'E', dirdeg=90)
add(P(f'오른쪽 그림과 같은 {tri("ABC")}에서 {ov("AD")}는 {ang("A")}의 이등분선이고, 꼭짓점 C를 지나 {ov("AD")}에 평행한 직선이 {ov("BA")}의 연장선과 만나는 점을 E라고 할 때, 다음을 구하시오.'
      f'<div class="sub">(1) {ov("AE")}의 길이</div><div class="sub">(2) {ov("DC")}의 길이</div>',
      None, f.render(),
      ans='(1) 10 cm (2) 6 cm',
      sol=f'<p>(1) {ov("AD")}{PAR}{ov("EC")}이므로 {ang("BAD")}={ang("AEC")} (동위각), {ang("DAC")}={ang("ACE")} (엇각)</p>'
          f'<p>{ang("BAD")}={ang("DAC")}이므로 {ang("AEC")}={ang("ACE")} → {ov("AE")}={ov("AC")}=10 cm</p>'
          f'<p>(2) {tri("BCE")}에서 {ov("AD")}{PAR}{ov("EC")}이므로 {ov("BD")} : {ov("DC")}={ov("BA")} : {ov("AE")}=15 : 10=3 : 2</p>'
          f'<p>9 : {ov("DC")}=3 : 2 ∴ {ov("DC")}=6 cm</p>'))

# 32 ------------------------------------------------------------------
A, D, B, C = (2.0, 6.0), (11.0, 6.0), (0.0, 0.0), (18.0, 0.0)
E, Fq = lerp(A, B, 2 / 3), lerp(D, C, 2 / 3)
Mq = inter(E, Fq, B, D)
Nq = inter(E, Fq, A, C)
f = Fig(150, 85)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D).seg(E, Fq)
f.arrow_mark(A, D).arrow_mark(E, Fq, t=0.9).arrow_mark(B, C)
f.dim(A, D, '9 cm', side=1, off=8, size=8.5).dim(B, C, '18 cm', side=1, off=-9, size=8.5)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=60)
f.label(E, 'E', dirdeg=180).label(Fq, 'F', dirdeg=0).label(Mq, 'M', dirdeg=110, dist_px=9).label(Nq, 'N', dirdeg=70, dist_px=9)
add(P(f'오른쪽 그림과 같은 사다리꼴 ABCD에서 {ov("AD")}{PAR}{ov("EF")}{PAR}{ov("BC")}이고, {ov("AD")}=9 cm, {ov("BC")}=18 cm이다. {ov("AE")}=2{ov("EB")}이고 {ov("EF")}와 {ov("BD")}, {ov("AC")}의 교점을 각각 M, N이라고 할 때, {ov("MN")}의 길이를 구하시오.',
      None, f.render(),
      ans='9 cm',
      sol=f'<p>{ov("AE")} : {ov("EB")}=2 : 1</p>'
          f'<p>{tri("ABC")}에서 {ov("EN")} : {ov("BC")}={ov("AE")} : {ov("AB")}=2 : 3이므로 {ov("EN")}=12 cm</p>'
          f'<p>{tri("BDA")}에서 {ov("EM")} : {ov("AD")}={ov("BE")} : {ov("BA")}=1 : 3이므로 {ov("EM")}=3 cm</p>'
          f'<p>∴ {ov("MN")}={ov("EN")}−{ov("EM")}=12−3=9(cm)</p>'))

# 33 ------------------------------------------------------------------
A, B, C = (5.0, 12.0), (0.0, 0.0), (14.0, 0.0)
D = lerp(B, C, 0.5)
G = centroid3(A, B, C)
E = inter(G, (G[0] + 1, G[1]), A, B)
Fq = inter(G, (G[0] + 1, G[1]), A, C)
f = Fig(130, 110)
f.poly([G, D, Fq], fill='#f9d7e2', stroke='#222')
f.segs(A, B, C, close=True).seg(A, D).seg(E, Fq).dot(G, 1.4)
f.arrow_mark(E, G, t=0.5).arrow_mark(B, D, t=0.5)
L3(f, A, B, C).label(D, 'D', dirdeg=-90).label(E, 'E', dirdeg=180).label(Fq, 'F', dirdeg=0).label(G, 'G', dirdeg=110, dist_px=8)
add(P(f'오른쪽 그림에서 점 G는 {tri("ABC")}의 무게중심이고, 점 G를 지나고 {ov("BC")}에 평행한 직선이 {ov("AB")}, {ov("AC")}와 만나는 점을 각각 E, F라고 하자. {tri("ABC")}=54 {CM2}일 때, {tri("GDF")}의 넓이를 구하시오.',
      None, f.render(),
      ans='6 ' + CM2,
      sol=f'<p>{tri("ADC")}={fr(1, 2)}{tri("ABC")}=27({CM2})</p>'
          f'<p>{ov("GF")}{PAR}{ov("DC")}이므로 {ov("AF")} : {ov("AC")}={ov("AG")} : {ov("AD")}=2 : 3 → {tri("ADF")}={fr(2, 3)}×27=18({CM2})</p>'
          f'<p>{ov("AG")} : {ov("GD")}=2 : 1이므로 {tri("GDF")}={fr(1, 3)}{tri("ADF")}=6({CM2})</p>'))

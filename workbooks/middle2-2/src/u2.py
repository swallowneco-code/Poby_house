"""2. 사각형의 성질"""
import math
from common import *  # noqa: F401,F403

PROBLEMS = []
add = PROBLEMS.append


def pgram(base, side, deg):
    """평행사변형 ABCD. B=(0,0), C=(base,0), ∠B=deg, AB=side."""
    B = (0.0, 0.0)
    C = (float(base), 0.0)
    A = polar(side, deg)
    D = (A[0] + base, A[1])
    return A, B, C, D


def L4(f, A, B, C, D, extra=None):
    f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
    return f


# 01 ------------------------------------------------------------------
A, B, C, D = pgram(5, 3, 62)
f = Fig(130, 90)
f.segs(A, B, C, D, close=True)
f.dim(B, A, f'3{sv("x")}+1', side=1, off=14, size=9)
f.dim(B, C, f'5{sv("x")}−6', side=1, off=-11, size=9)
f.dim(C, D, f'4{sv("x")}−3', side=1, off=-15, size=9)
L4(f, A, B, C, D)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ov("AB")}=3{v("x")}+1, {ov("BC")}=5{v("x")}−6, {ov("CD")}=4{v("x")}−3일 때, {ov("AD")}의 길이를 구하시오.',
      None, f.render(),
      ans='14',
      sol=f'<p>평행사변형의 두 쌍의 대변의 길이는 각각 같으므로 {ov("AB")}={ov("CD")}</p>'
          f'<p>3{v("x")}+1=4{v("x")}−3 ∴ {v("x")}=4</p>'
          f'<p>{ov("AD")}={ov("BC")}=5×4−6=14</p>'))

# 02 ------------------------------------------------------------------
A, B, C, D = pgram(5, 3, 70)
f = Fig(120, 85)
f.segs(A, B, C, D, close=True).arc(C, B, D, 9, fill='#f6c48f')
L4(f, A, B, C, D)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ang("A")} : {ang("B")}=7 : 5일 때, {ang("C")}의 크기를 구하시오.',
      None, f.render(),
      ans='105°',
      sol=f'<p>{ang("A")}+{ang("B")}=180°이므로 {ang("A")}=180°×{fr(7, 12)}=105°</p>'
          f'<p>평행사변형의 두 쌍의 대각의 크기는 각각 같으므로 {ang("C")}={ang("A")}=105°</p>'))

# 03 ------------------------------------------------------------------
A, B, C, D = pgram(5, 3, 62)
O = lerp(A, C, 0.5)
f = Fig(120, 85)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D)
L4(f, A, B, C, D).label(O, 'O', dirdeg=-90, dist_px=9)
add(P(f'오른쪽 그림에서 {sq("ABCD")}는 평행사변형이고, 점 O는 두 대각선의 교점이다. 다음 <b>보기</b>에서 옳은 것을 모두 고르시오.'
      f'<div class="box"><div class="bt">보기</div>'
      f'ㄱ. {ov("BO")}={ov("DO")}　　　ㄴ. {ov("AC")}={ov("BD")}<br>'
      f'ㄷ. {tri("AOD")}{CONG}{tri("COB")}　ㄹ. {ov("AB")}={ov("AD")}<br>'
      f'ㅁ. {ang("ABD")}={ang("CBD")}　ㅂ. {ang("BAD")}+{ang("ABC")}=180°</div>',
      None, f.render(),
      ans='ㄱ, ㄷ, ㅂ',
      sol=f'<p>ㄱ. 평행사변형의 두 대각선은 서로를 이등분하므로 {ov("BO")}={ov("DO")}</p>'
          f'<p>ㄷ. {ov("AO")}={ov("CO")}, {ov("DO")}={ov("BO")}, {ang("AOD")}={ang("COB")}(맞꼭지각)이므로 SAS 합동</p>'
          f'<p>ㅂ. 이웃하는 두 내각의 크기의 합은 180°이다.</p>'
          f'<p>ㄴ은 직사각형, ㄹ, ㅁ은 마름모일 때 성립한다. 따라서 옳은 것은 ㄱ, ㄷ, ㅂ이다.</p>'))

# 04 ------------------------------------------------------------------
B = (0.0, 0.0)
A = polar(7, 72)
D = (A[0] + 10, A[1])
C = (10.0, 0.0)
bis = polar(1, -(180 - 72) / 2, A)
E = inter(A, bis, D, C)
Fp = inter(A, E, B, C)
f = Fig(130, 120)
f.segs(A, B, C, D, close=True).seg(C, E).seg(A, E)
f.arc(A, B, Fp, 11).arc(A, Fp, D, 14)
f.dim(B, A, '7 cm', side=1, off=12, size=9).dim(A, D, '10 cm', side=1, off=9, size=9)
L4(f, A, B, C, D).label(E, 'E', dirdeg=-60)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ang("A")}의 이등분선이 {ov("DC")}의 연장선과 만나는 점을 E라고 하자. {ov("AB")}=7 cm, {ov("AD")}=10 cm일 때, {ov("CE")}의 길이를 구하시오.',
      None, f.render(),
      ans='3 cm',
      sol=f'<p>{ov("AB")}{PAR}{ov("DE")}이므로 {ang("BAE")}={ang("AED")} (엇각)</p>'
          f'<p>{ang("DAE")}={ang("BAE")}이므로 {ang("DAE")}={ang("DEA")} → {tri("DAE")}는 이등변삼각형</p>'
          f'<p>{ov("DE")}={ov("DA")}=10 cm, {ov("DC")}={ov("AB")}=7 cm ∴ {ov("CE")}=10−7=3(cm)</p>'))

# 05 ------------------------------------------------------------------
B = (0.0, 0.0)
A = polar(4, 110)
C = (6.0, 0.0)
D = (A[0] + 6, A[1])
E = inter(A, polar(1, -35, A), B, C)
H = foot(D, A, E)
f = Fig(140, 100)
f.segs(A, B, C, D, close=True).seg(A, E).seg(D, H).right(H, D, E, 5)
f.arc(A, B, E, 11).arc(A, E, D, 14)
f.arc(C, B, D, 10, label='70°', lsize=8.5, ldist=21)
f.arc(D, C, H, 10, fill='#f6c48f')
L4(f, A, B, C, D).label(E, 'E', dirdeg=-90).label(H, 'H', dirdeg=200, dist_px=9)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ang("A")}의 이등분선이 {ov("BC")}와 만나는 점을 E라 하고, 꼭짓점 D에서 {ov("AE")}에 내린 수선의 발을 H라고 하자. {ang("C")}=70°일 때, {ang("CDH")}의 크기를 구하시오.',
      None, f.render(),
      ans='55°',
      sol=f'<p>{ang("BAD")}={ang("C")}=70°이므로 {ang("DAH")}=35°</p>'
          f'<p>{tri("AHD")}에서 {ang("ADH")}=180°−(90°+35°)=55°</p>'
          f'<p>{ang("ADC")}=180°−70°=110°이므로 {ang("CDH")}=110°−55°=55°</p>'))

# 06 ------------------------------------------------------------------
A, B, C, D = pgram(5, 3, 65)
O = lerp(A, C, 0.5)
Pp = lerp(A, B, 0.3)
Q = (2 * O[0] - Pp[0], 2 * O[1] - Pp[1])
f = Fig(130, 90)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D).seg(Pp, Q)
L4(f, A, B, C, D).label(O, 'O', dirdeg=-90, dist_px=9).label(Pp, 'P', dirdeg=180).label(Q, 'Q', dirdeg=0)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD의 두 대각선의 교점 O를 지나는 직선이 {ov("AB")}, {ov("CD")}와 만나는 점을 각각 P, Q라고 할 때, 다음 중 옳지 <u>않은</u> 것은?',
      [f'{ov("OB")}={ov("OD")}', f'{tri("OBP")}{CONG}{tri("ODQ")}', f'{ang("OAP")}={ang("OCQ")}', f'{ov("OA")}={ov("OB")}', f'{ov("AP")}={ov("CQ")}'],
      f.render(),
      ans='④',
      sol=f'<p>① 평행사변형의 두 대각선은 서로를 이등분한다.</p>'
          f'<p>③ {ov("AB")}{PAR}{ov("DC")}이므로 엇각으로 같다.</p>'
          f'<p>② {ov("OB")}={ov("OD")}, {ang("OBP")}={ang("ODQ")}(엇각), {ang("BOP")}={ang("DOQ")}(맞꼭지각)이므로 ASA 합동</p>'
          f'<p>⑤ 같은 방법으로 {tri("OAP")}{CONG}{tri("OCQ")}이므로 {ov("AP")}={ov("CQ")}</p>'
          f'<p>④ {ov("OA")}={ov("OB")}는 두 대각선의 길이가 같은 경우에만 성립한다.</p>'))

# 07 ------------------------------------------------------------------
add(P(f'다음 중 {sq("ABCD")}가 평행사변형인 것은? (단, 점 O는 두 대각선의 교점이다.)',
      [f'{ov("AB")}={ov("AD")}=4 cm, {ov("BC")}={ov("CD")}=6 cm', f'{ov("AB")}={ov("DC")}=6 cm, {ov("AD")}={ov("BC")}=8 cm',
       f'{ov("OA")}={ov("OB")}=3 cm, {ov("OC")}={ov("OD")}=5 cm', f'{ov("AB")}{PAR}{ov("DC")}, {ov("AD")}={ov("BC")}=5 cm',
       f'{ang("A")}={ang("B")}=95°, {ang("C")}=85°'],
      ans='②',
      sol=f'<p>② 두 쌍의 대변의 길이가 각각 같으므로 평행사변형이다.</p>'
          f'<p>① 이웃하는 변끼리 길이가 같은 것이므로 평행사변형이 아니다.</p>'
          f'<p>③ 두 대각선이 서로를 이등분하지 않는다.</p>'
          f'<p>④ 한 쌍의 대변이 평행하고 다른 한 쌍의 대변의 길이가 같으면 등변사다리꼴일 수도 있다.</p>'
          f'<p>⑤ {ang("D")}=360°−(95°+95°+85°)=85°이므로 두 쌍의 대각의 크기가 각각 같지 않다.</p>'))

# 08 ------------------------------------------------------------------
B = (0.0, 0.0)
A = polar(10, 60)
C = (14.0, 0.0)
D = (A[0] + 14, A[1])
E = (10.0, 0.0)
F = (A[0] + 4, A[1])
f = Fig(150, 95)
f.segs(A, B, C, D, close=True).seg(A, E).seg(C, F)
f.arc(A, B, E, 10).arc(A, E, F, 13).arc(C, F, D, 10).arc(C, E, F, 13)
f.arc(B, C, A, 9, label='60°', lsize=8.5, ldist=20)
f.dim(B, A, '10 cm', side=1, off=12, size=9).dim(A, D, '14 cm', side=1, off=9, size=9)
L4(f, A, B, C, D).label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=90)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ang("A")}, {ang("C")}의 이등분선이 {ov("BC")}, {ov("AD")}와 만나는 점을 각각 E, F라고 하자. {ov("AB")}=10 cm, {ov("AD")}=14 cm, {ang("B")}=60°일 때, {sq("AECF")}의 둘레의 길이를 구하시오.',
      None, f.render(),
      ans='28 cm',
      sol=f'<p>{ang("BAD")}=180°−60°=120°이므로 {ang("BAE")}=60°, {ang("AEB")}=180°−(60°+60°)=60°</p>'
          f'<p>{tri("ABE")}는 정삼각형이므로 {ov("AE")}={ov("BE")}=10 cm, {ov("EC")}=14−10=4(cm)</p>'
          f'<p>{ang("AEC")}={ang("AFC")}=120°, {ang("EAF")}={ang("ECF")}=60°로 두 쌍의 대각의 크기가 각각 같으므로 {sq("AECF")}는 평행사변형이다.</p>'
          f'<p>(둘레의 길이)=2×(10+4)=28(cm)</p>'))

# 09 ------------------------------------------------------------------
A, B, C, D = pgram(6, 4, 65)
Pp = (3.1, 2.4)
f = Fig(120, 90)
f.poly([A, B, C, D], fill='#fbe3b0', stroke='#222')
f.poly([A, Pp, D], fill='#ffffff', stroke='#222').poly([B, Pp, C], fill='#ffffff', stroke='#222')
f.seg(A, Pp).seg(B, Pp).seg(C, Pp).seg(D, Pp).dot(Pp, 1.4)
L4(f, A, B, C, D).label(Pp, 'P', dirdeg=90, dist_px=9)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD의 내부의 한 점 P에 대하여 {tri("PAD")}와 {tri("PBC")}의 넓이의 합이 23 {CM2}일 때, {sq("ABCD")}의 넓이를 구하시오.',
      None, f.render(),
      ans='46 ' + CM2,
      sol=f'<p>평행사변형의 내부의 한 점 P에 대하여 {tri("PAD")}+{tri("PBC")}={fr(1, 2)}{sq("ABCD")}</p>'
          f'<p>(점 P에서 {ov("AD")}, {ov("BC")}까지의 거리의 합이 평행사변형의 높이와 같기 때문이다.)</p>'
          f'<p>∴ {sq("ABCD")}=2×23=46({CM2})</p>'))

# 10 ------------------------------------------------------------------
w, h = 10.0, 10.0 * math.tan(math.radians(28))
A, B, C, D = (0, h), (0, 0), (w, 0), (w, h)
O = (w / 2, h / 2)
f = Fig(140, 85)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D)
f.arc(O, A, B, 9, label='56°', lsize=8.5, ldist=20)
f.arc(D, O, C, 12, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=20)
f.arc(B, C, O, 15, fill='#cfe3f7', label='<tspan font-style="italic">y</tspan>', ldist=24)
L4(f, A, B, C, D).label(O, 'O', dirdeg=90, dist_px=9)
add(P(f'오른쪽 그림과 같은 직사각형 ABCD에서 두 대각선의 교점을 O라고 하자. {ang("AOB")}=56°이고 {ang("ODC")}={ang(v("x"))}, {ang("OBC")}={ang(v("y"))}일 때, {ang(v("x"))}−{ang(v("y"))}의 크기는?',
      ['28°', '30°', '32°', '34°', '36°'], f.render(),
      ans='④',
      sol=f'<p>직사각형의 두 대각선은 길이가 같고 서로를 이등분하므로 {ov("OA")}={ov("OB")}</p>'
          f'<p>{ang("OBA")}=(180°−56°)÷2=62°이므로 {ang(v("y"))}=90°−62°=28°</p>'
          f'<p>{ov("AB")}{PAR}{ov("DC")}이므로 {ang(v("x"))}={ang("ODC")}={ang("OBA")}=62° (엇각)</p>'
          f'<p>∴ {ang(v("x"))}−{ang(v("y"))}=62°−28°=34°</p>'))

# 11 ------------------------------------------------------------------
hh = 4.0
ww = hh * math.sqrt(3)
A, B, C, D = (0, hh), (0, 0), (ww, 0), (ww, hh)
E = (hh * math.tan(math.radians(30)), hh)
F = (ww - E[0], 0)
f = Fig(140, 90)
f.segs(A, B, C, D, close=True).seg(B, E).seg(B, D).seg(D, F)
f.arc(B, E, A, 11).arc(B, D, E, 14).arc(D, F, C, 11).arc(D, B, F, 14)
f.arc(E, A, B, 9, fill='#f6c48f')
L4(f, A, B, C, D).label(E, 'E', dirdeg=90).label(F, 'F', dirdeg=-90)
add(P(f'오른쪽 그림과 같은 직사각형 ABCD에서 {ang("ABD")}의 이등분선과 {ov("AD")}의 교점을 E, {ang("BDC")}의 이등분선과 {ov("BC")}의 교점을 F라고 하자. {sq("EBFD")}가 마름모일 때, {ang("AEB")}의 크기를 구하시오.',
      None, f.render(),
      ans='60°',
      sol=f'<p>{sq("EBFD")}가 마름모이므로 {ov("EB")}={ov("ED")} → {ang("EBD")}={ang("EDB")}</p>'
          f'<p>{ov("AD")}{PAR}{ov("BC")}이므로 {ang("EDB")}={ang("DBC")} (엇각)</p>'
          f'<p>따라서 {ang("ABE")}={ang("EBD")}={ang("DBC")}이고 세 각의 합이 90°이므로 각각 30°</p>'
          f'<p>{tri("ABE")}에서 {ang("AEB")}=90°−30°=60°</p>'))

# 12 ------------------------------------------------------------------
b = 3.0
a = b * math.tan(math.radians(40))
O = (0.0, 0.0)
B, D = polar(b, 200), polar(b, 20)
C, A = polar(a, 290), polar(a, 110)
f = Fig(130, 100)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D)
f.arc(A, D, C, 10, label='50°', lsize=8.5, ldist=21)
f.arc(B, C, D, 14, label='40°', lsize=8.5, ldist=26)
f.arc(D, B, C, 10, fill='#f6c48f')
f.label(A, 'A', dirdeg=100).label(B, 'B', dirdeg=190).label(C, 'C', dirdeg=-80).label(D, 'D', dirdeg=10).label(O, 'O', dirdeg=-60, dist_px=10)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 점 O는 두 대각선의 교점이고 {ang("DAC")}=50°, {ang("DBC")}=40°일 때, {ang("BDC")}의 크기를 구하시오.',
      None, f.render(),
      ans='40°',
      sol=f'<p>{ov("AD")}{PAR}{ov("BC")}이므로 {ang("ACB")}={ang("DAC")}=50° (엇각)</p>'
          f'<p>{tri("OBC")}에서 {ang("BOC")}=180°−(40°+50°)=90°이므로 두 대각선이 서로 수직이다.</p>'
          f'<p>따라서 {sq("ABCD")}는 마름모이고, 마름모의 대각선은 내각을 이등분하므로</p>'
          f'<p>{ang("BDC")}={ang("BDA")}={ang("DBC")}=40°</p>'))

# 13 ------------------------------------------------------------------
A, B, C, D = (0, 10), (0, 0), (10, 0), (10, 10)
O = (5, 5)
G = polar(10, -20, O)
E = polar(10, -110, O)
F = (E[0] + G[0] - O[0], E[1] + G[1] - O[1])
Pp = inter(O, E, B, C)
Q = inter(O, G, C, D)
f = Fig(140, 140)
f.poly([O, Pp, C, Q], fill='#bfe3ef')
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D).segs(O, E, F, G, close=True)
f.dim(A, B, '10 cm', side=1, off=-16, size=9)
L4(f, A, B, C, D).label(O, 'O', dirdeg=90, dist_px=9).label(Pp, 'P', dirdeg=-160).label(Q, 'Q', dirdeg=20)
f.label(E, 'E', dirdeg=200).label(F, 'F', dirdeg=-60).label(G, 'G', dirdeg=0)
add(P(f'오른쪽 그림과 같이 한 변의 길이가 10 cm인 정사각형 ABCD의 두 대각선의 교점을 O라고 하자. 정사각형 ABCD와 정사각형 OEFG가 합동일 때, {sq("OPCQ")}의 넓이는?',
      ['20 ' + CM2, '22 ' + CM2, '24 ' + CM2, '25 ' + CM2, '30 ' + CM2], f.render(),
      ans='④',
      sol=f'<p>{tri("OBP")}와 {tri("OCQ")}에서 {ov("OB")}={ov("OC")}, {ang("OBP")}={ang("OCQ")}=45°</p>'
          f'<p>{ang("BOP")}=90°−{ang("POC")}={ang("COQ")}이므로 {tri("OBP")}{CONG}{tri("OCQ")} (ASA 합동)</p>'
          f'<p>{sq("OPCQ")}={tri("OPC")}+{tri("OCQ")}={tri("OPC")}+{tri("OBP")}={tri("OBC")}</p>'
          f'<p>={fr(1, 4)}×10×10=25({CM2})</p>'))

# 14 ------------------------------------------------------------------
hgt = 3 * math.tan(math.radians(70))
B, C = (0.0, 0.0), (13.0, 0.0)
A, D = (3.0, hgt), (10.0, hgt)
E = (3.0, 0.0)
f = Fig(150, 90)
f.segs(A, B, C, D, close=True).seg(A, E).right(E, A, C, 5)
f.arc(A, B, E, 12, fill='#f6c48f', label=f'<tspan font-style="italic">y</tspan>°', ldist=22)
f.arc(C, D, B, 10, label='70°', lsize=8.5, ldist=21)
f.dim(A, D, '7 cm', side=1, off=9, size=9).dim(B, C, '13 cm', side=1, off=-10, size=9, t=0.6)
f.dim(B, E, f'{sv("x")} cm', side=1, off=-13, size=9, curve=True)
L4(f, A, B, C, D).label(E, 'E', dirdeg=-30, dist_px=9)
add(P(f'오른쪽 그림과 같이 {ov("AD")}{PAR}{ov("BC")}인 등변사다리꼴 ABCD의 꼭짓점 A에서 {ov("BC")}에 내린 수선의 발을 E라고 하자. {ov("AD")}=7 cm, {ov("BC")}=13 cm, {ang("C")}=70°이고 {ov("BE")}={v("x")} cm, {ang("BAE")}={v("y")}°일 때, {v("x")}+{v("y")}의 값을 구하시오.',
      None, f.render(),
      ans='23',
      sol=f'<p>점 D에서 {ov("BC")}에 내린 수선의 발을 F라고 하면 {tri("ABE")}{CONG}{tri("DCF")} (RHA 합동)이고 {ov("EF")}={ov("AD")}=7 cm</p>'
          f'<p>{ov("BE")}=(13−7)÷2=3(cm) ∴ {v("x")}=3</p>'
          f'<p>등변사다리꼴이므로 {ang("B")}={ang("C")}=70°, {ang("BAE")}=90°−70°=20° ∴ {v("y")}=20</p>'
          f'<p>∴ {v("x")}+{v("y")}=23</p>'))

# 15 ------------------------------------------------------------------
add(P(f'다음을 모두 만족시키는 {sq("ABCD")}는 어떤 사각형인지 말하시오.'
      f'<div class="box">(가) {ov("AB")}{PAR}{ov("DC")}　　　(나) {ov("AD")}{PAR}{ov("BC")}<br>'
      f'(다) {ov("AC")}{PERP}{ov("BD")}　　　(라) {ang("A")}=90°</div>',
      None,
      ans='정사각형',
      sol=f'<p>(가), (나)에서 두 쌍의 대변이 각각 평행하므로 평행사변형</p>'
          f'<p>(다)에서 두 대각선이 서로 수직인 평행사변형이므로 마름모</p>'
          f'<p>(라)에서 한 내각이 직각인 마름모이므로 정사각형이다.</p>'))

# 16 ------------------------------------------------------------------
add(P('다음 <b>보기</b> 중 두 대각선의 길이가 같은 사각형을 모두 고르시오.'
      '<div class="box"><div class="bt">보기</div>ㄱ. 사다리꼴　　ㄴ. 평행사변형　　ㄷ. 직사각형<br>ㄹ. 마름모　　　ㅁ. 정사각형　　　ㅂ. 등변사다리꼴</div>',
      None,
      ans='ㄷ, ㅁ, ㅂ',
      sol='<p>두 대각선의 길이가 같은 사각형은 직사각형, 정사각형, 등변사다리꼴이다.</p>'
          '<p>사다리꼴, 평행사변형, 마름모는 두 대각선의 길이가 같다고 할 수 없다.</p>'))

# 17 ------------------------------------------------------------------
add(P('다음 중 직사각형의 각 변의 중점을 연결하여 만든 사각형의 성질이 <u>아닌</u> 것을 모두 고르면? (정답 2개)',
      ['네 변의 길이가 모두 같다.', '두 쌍의 대변이 각각 평행하다.', '두 대각선의 길이가 같다.', '두 대각선이 서로 수직이다.', '네 내각의 크기가 모두 같다.'],
      ans='③, ⑤',
      sol='<p>직사각형의 각 변의 중점을 연결하여 만든 사각형은 마름모이다.</p>'
          '<p>마름모는 네 변의 길이가 같고, 두 쌍의 대변이 각각 평행하며, 두 대각선이 서로를 수직이등분한다.</p>'
          '<p>③ 두 대각선의 길이가 같은 것, ⑤ 네 내각의 크기가 같은 것은 마름모의 성질이 아니다.</p>'))

# 18 ------------------------------------------------------------------
B, A, C, E = (0, 0), (0, 6), (4, 0), (10, 0)
D = (4, 9)
f = Fig(140, 115)
f.poly([A, B, C, D], fill='#fbf1b7', stroke='#222')
f.seg(A, C).seg(C, E).seg(D, E).seg(C, D).right(B, A, C, 5)
f.arrow_mark(A, C).arrow_mark(D, E)
f.dim(B, A, '6 cm', side=1, off=12, size=9).dim(B, C, '4 cm', side=1, off=-10, size=9).dim(C, E, '6 cm', side=1, off=-10, size=9)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=90).label(E, 'E', dirdeg=-30)
add(P(f'오른쪽 그림에서 {ov("AC")}{PAR}{ov("DE")}, {ov("AB")}{PERP}{ov("BE")}이고 {ov("AB")}=6 cm, {ov("BC")}=4 cm, {ov("CE")}=6 cm일 때, {sq("ABCD")}의 넓이는?',
      ['26 ' + CM2, '28 ' + CM2, '30 ' + CM2, '32 ' + CM2, '34 ' + CM2], f.render(),
      ans='③',
      sol=f'<p>{ov("AC")}{PAR}{ov("DE")}이므로 {tri("ACD")}={tri("ACE")} (밑변 {ov("AC")}가 같고 높이가 같다.)</p>'
          f'<p>{sq("ABCD")}={tri("ABC")}+{tri("ACD")}={tri("ABC")}+{tri("ACE")}={tri("ABE")}</p>'
          f'<p>={fr(1, 2)}×(4+6)×6=30({CM2})</p>'))

# 19 ------------------------------------------------------------------
B, C = (0.0, 0.0), (10.0, 0.0)
A = (6.4, 4.8)
Fp, G = (0.0, -10.0), (10.0, -10.0)
Ep, Dp = outer_square(B, A, C)
Hp, Ip = outer_square(C, A, B)
f = Fig(150, 160)
f.poly([A, G, C], fill='#cfe8c0', stroke='#222')
f.segs(A, B, C, close=True).segs(B, Ep, Dp, A).segs(A, Ip, Hp, C).segs(B, Fp, G, C)
f.right(A, B, C, 5)
f.dim(B, A, '8 cm', side=1, off=10, size=9).dim(B, C, '10 cm', side=1, off=-9, size=9, t=0.35)
f.label(A, 'A', dirdeg=70).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=10)
f.label(Dp, 'D', dirdeg=90).label(Ep, 'E', dirdeg=180).label(Fp, 'F', dirdeg=225).label(G, 'G', dirdeg=-45)
f.label(Hp, 'H', dirdeg=0).label(Ip, 'I', dirdeg=60)
add(P(f'오른쪽 그림은 {ang("A")}=90°인 직각삼각형 ABC의 세 변을 각각 한 변으로 하는 세 정사각형을 그린 것이다. {ov("AB")}=8 cm, {ov("BC")}=10 cm일 때, {tri("AGC")}의 넓이를 구하시오.',
      None, f.render(),
      ans='18 ' + CM2,
      sol=f'<p>{ov("AC")}<sup>2</sup>=10<sup>2</sup>−8<sup>2</sup>=36 ∴ {ov("AC")}=6 cm</p>'
          f'<p>{tri("ACG")}와 {tri("HCB")}에서 {ov("CA")}={ov("CH")}, {ov("CG")}={ov("CB")}, {ang("ACG")}=90°+{ang("ACB")}={ang("HCB")}이므로 {tri("ACG")}{CONG}{tri("HCB")} (SAS 합동)</p>'
          f'<p>{ov("AB")}{PAR}{ov("CH")}이므로 {tri("HCB")}={tri("HCA")}={fr(1, 2)}{sq("ACHI")}</p>'
          f'<p>{tri("AGC")}={fr(1, 2)}×{ov("AC")}<sup>2</sup>={fr(1, 2)}×36=18({CM2})</p>'))

# 20 ------------------------------------------------------------------
A, B, C, D = (1.5, 4.5), (0, 0), (8, 0), (9.5, 4.5)
E = (6, 0)
F = inter(A, E, D, C)
f = Fig(130, 120)
f.poly([B, F, E], fill='#cfe3f7', stroke='#222')
f.segs(A, B, C, D, close=True).seg(E, F).seg(C, F).seg(A, E)
L4(f, A, B, C, D).label(E, 'E', dirdeg=110, dist_px=9).label(F, 'F', dirdeg=-60)
add(P(f'오른쪽 그림과 같이 넓이가 64 {CM2}인 평행사변형 ABCD에서 {ov("BE")} : {ov("EC")}=3 : 1이 되도록 점 E를 잡고, {ov("AE")}의 연장선과 {ov("DC")}의 연장선의 교점을 F라고 하자. 이때 {tri("BFE")}의 넓이를 구하시오.',
      None, f.render(),
      ans='8 ' + CM2,
      sol=f'<p>{tri("ABC")}={fr(1, 2)}×64=32({CM2})이므로 {tri("ABE")}=32×{fr(3, 4)}=24({CM2})</p>'
          f'<p>{ov("AB")}{PAR}{ov("CF")}이므로 {tri("ABE")}∽{tri("FCE")}이고 {ov("AE")} : {ov("EF")}={ov("BE")} : {ov("EC")}=3 : 1</p>'
          f'<p>{tri("ABE")}와 {tri("FBE")}는 높이가 같으므로 넓이의 비는 {ov("AE")} : {ov("EF")}=3 : 1</p>'
          f'<p>∴ {tri("BFE")}=24×{fr(1, 3)}=8({CM2})</p>'))

# 21 예제 1 ------------------------------------------------------------
def _bis_fig(bdeg, mark_e, mark_f, e_label, f_label):
    B = (0.0, 0.0)
    A = polar(5, bdeg)
    C = (8.0, 0.0)
    D = (A[0] + 8, A[1])
    E = inter(A, polar(1, -(180 - bdeg) / 2, A), B, C)
    F = inter(B, polar(1, bdeg / 2), A, D)
    f = Fig(150, 100)
    f.segs(A, B, C, D, close=True).seg(A, E).seg(B, F)
    f.arc(A, B, E, 10).arc(A, E, D, 13).arc(B, C, F, 10, double=True).arc(B, F, A, 13, double=True)
    if mark_e:
        f.arc(E, A, C, 10, fill='#f6c48f' if e_label.startswith('<') else None, label=e_label, lsize=8.5, ldist=21)
    if mark_f:
        f.arc(F, B, D, 10, fill='#f6c48f' if f_label.startswith('<') else None, label=f_label, lsize=8.5, ldist=21)
    L4(f, A, B, C, D).label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=90)
    return f.render()

xs = '<tspan font-style="italic">x</tspan>'
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ov("AE")}, {ov("BF")}는 각각 {ang("A")}, {ang("B")}의 이등분선이다. {ang("AEC")}=140°이고 {ang("BFD")}={ang(v("x"))}일 때, {ang(v("x"))}의 크기를 구하시오.',
      None, _bis_fig(100, True, True, '140°', xs), tag='예제',
      ans='130°',
      sol=f'<p>{ang("AEB")}=180°−140°=40°이고 {ang("FAE")}={ang("AEB")}=40° (엇각)이므로 {ang("FAB")}=2×40°=80°</p>'
          f'<p>{ang("ABE")}=180°−80°=100°이므로 {ang("ABF")}={fr(1, 2)}×100°=50°</p>'
          f'<p>{tri("ABF")}에서 {ang(v("x"))}={ang("BAF")}+{ang("ABF")}=80°+50°=130°</p>'))

# 22 유제 1 ------------------------------------------------------------
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ang("A")}, {ang("B")}의 이등분선이 {ov("BC")}, {ov("AD")}와 만나는 점을 각각 E, F라고 하자. {ang("BFD")}=125°이고 {ang("AEC")}={ang(v("x"))}일 때, {ang(v("x"))}의 크기를 구하시오.',
      None, _bis_fig(110, True, True, xs, '125°'), tag='유제',
      ans='145°',
      sol=f'<p>{ang("AFB")}=180°−125°=55°이고 {ang("FBE")}={ang("AFB")}=55° (엇각)이므로 {ang("ABE")}=2×55°=110°</p>'
          f'<p>{ang("BAD")}=180°−110°=70°이므로 {ang("BAE")}={fr(1, 2)}×70°=35°</p>'
          f'<p>{tri("ABE")}에서 {ang(v("x"))}={ang("ABE")}+{ang("BAE")}=110°+35°=145°</p>'))

# 23 예제 2 ------------------------------------------------------------
A, B, C, D = (0, 10), (0, 0), (10, 0), (10, 10)
be = 10 / math.tan(math.radians(70))
E, F = (be, 0), (10, be)
f = Fig(125, 125)
f.segs(A, B, C, D, close=True).seg(A, E).seg(B, F).tick(B, E).tick(C, F)
f.arc(E, A, C, 9, label='110°', lsize=8.5, ldist=21)
f.arc(B, C, F, 14, fill='#f6c48f')
L4(f, A, B, C, D).label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=0)
add(P(f'오른쪽 그림과 같은 정사각형 ABCD에서 {ov("BC")}와 {ov("CD")} 위에 {ov("BE")}={ov("CF")}가 되도록 두 점 E, F를 각각 잡았다. {ang("AEC")}=110°일 때, {ang("FBC")}의 크기를 구하시오.',
      None, f.render(), tag='예제',
      ans='20°',
      sol=f'<p>{tri("ABE")}와 {tri("BCF")}에서 {ov("AB")}={ov("BC")}, {ang("ABE")}={ang("BCF")}=90°, {ov("BE")}={ov("CF")}이므로 {tri("ABE")}{CONG}{tri("BCF")} (SAS 합동)</p>'
          f'<p>{ang("AEB")}=180°−110°=70°이므로 {ang("EAB")}=180°−(90°+70°)=20°</p>'
          f'<p>∴ {ang("FBC")}={ang("EAB")}=20°</p>'))

# 24 유제 2 ------------------------------------------------------------
be = 10 * math.tan(math.radians(28))
E, F = (be, 0), (10, be)
f = Fig(125, 125)
f.segs(A, B, C, D, close=True).seg(A, E).seg(B, F).tick(B, E).tick(C, F)
f.arc(A, D, E, 12, label='62°', lsize=8.5, ldist=24)
f.arc(F, B, D, 9, fill='#f6c48f')
L4(f, A, B, C, D).label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=0)
add(P(f'오른쪽 그림과 같은 정사각형 ABCD에서 {ov("BC")}와 {ov("CD")} 위에 {ov("BE")}={ov("CF")}가 되도록 두 점 E, F를 각각 잡았다. {ang("DAE")}=62°일 때, {ang("BFD")}의 크기를 구하시오.',
      None, f.render(), tag='유제',
      ans='118°',
      sol=f'<p>{tri("ABE")}{CONG}{tri("BCF")} (SAS 합동)</p>'
          f'<p>{ang("BAE")}=90°−62°=28°이므로 {ang("CBF")}={ang("BAE")}=28°</p>'
          f'<p>{tri("BCF")}에서 {ang("BFC")}=180°−(90°+28°)=62° ∴ {ang("BFD")}=180°−62°=118°</p>'))

# 25 ------------------------------------------------------------------
B = (0.0, 0.0)
A = polar(9, 78)
C = (7.0, 0.0)
D = (A[0] + 7, A[1])
E = lerp(D, C, 0.5)
F = inter(B, E, A, D)
f = Fig(140, 110)
f.segs(A, B, C, D, close=True).seg(D, F).seg(B, F).tick(D, E).tick(E, C)
f.dim(B, A, '9 cm', side=1, off=12, size=9).dim(B, C, '7 cm', side=1, off=-10, size=9)
L4(f, A, B, C, D).label(E, 'E', dirdeg=0).label(F, 'F', dirdeg=45)
add(P(f'오른쪽 그림과 같은 평행사변형 ABCD에서 {ov("DC")}의 중점을 E, {ov("BE")}의 연장선과 {ov("AD")}의 연장선이 만나는 점을 F라고 하자. {ov("AB")}=9 cm, {ov("BC")}=7 cm일 때, 다음을 구하시오.'
      f'<div class="sub">(1) {tri("DEF")}와 합동인 삼각형과 그 합동 조건</div><div class="sub">(2) {ov("AF")}의 길이</div>',
      None, f.render(),
      ans=f'(1) {tri("CEB")}, ASA 합동 (2) 14 cm',
      sol=f'<p>(1) {tri("DEF")}와 {tri("CEB")}에서 {ov("DE")}={ov("CE")}, {ang("DEF")}={ang("CEB")}(맞꼭지각), {ang("EDF")}={ang("ECB")}(엇각)</p>'
          f'<p>∴ {tri("DEF")}{CONG}{tri("CEB")} (ASA 합동)</p>'
          f'<p>(2) {ov("DF")}={ov("CB")}=7 cm, {ov("AD")}={ov("BC")}=7 cm이므로 {ov("AF")}=7+7=14(cm)</p>'))

# 26 ------------------------------------------------------------------
bb = 4.0
aa = bb * math.tan(math.radians(35))
O = (0.0, 0.0)
B, D, A, C = (-bb, 0.0), (bb, 0.0), (0.0, aa), (0.0, -aa)
H = foot(A, C, D)
Pp = inter(A, H, B, D)
f = Fig(175, 120)
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D).seg(A, H).right(H, A, D, 4).right(O, A, D, 4)
f.arc(B, D, C, 15, label='35°', lsize=8.5, ldist=27)
f.arc(A, C, H, 16, fill='#f6c48f', label=xs, ldist=25)
f.arc(Pp, A, D, 7, fill='#e7b3d6', label='<tspan font-style="italic">y</tspan>', ldist=14)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=180).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=0)
f.label(O, 'O', dirdeg=-135, dist_px=8).label(H, 'H', dirdeg=-30).label(Pp, 'P', dirdeg=-70, dist_px=9)
add(P(f'오른쪽 그림과 같은 마름모 ABCD에서 점 O는 두 대각선의 교점이고, 꼭짓점 A에서 {ov("CD")}에 내린 수선의 발을 H, {ov("AH")}와 {ov("BD")}의 교점을 P라고 하자. {ang("DBC")}=35°이고 {ang("OAH")}={ang(v("x"))}, {ang("APD")}={ang(v("y"))}일 때, {ang(v("x"))}+{ang(v("y"))}의 크기를 구하시오.',
      None, f.render(),
      ans='160°',
      sol=f'<p>마름모의 대각선은 내각을 이등분하고 서로 수직이므로 {ang("BDC")}={ang("DBC")}=35°, {ang("DOC")}=90°</p>'
          f'<p>{tri("OCD")}에서 {ang("OCD")}=90°−35°=55°</p>'
          f'<p>{tri("AHC")}에서 {ang(v("x"))}={ang("CAH")}=90°−55°=35°</p>'
          f'<p>{tri("APO")}에서 {ang("APO")}=90°−35°=55°이므로 {ang(v("y"))}={ang("APD")}=180°−55°=125°</p>'
          f'<p>∴ {ang(v("x"))}+{ang(v("y"))}=35°+125°=160°</p>'))

# 27 ------------------------------------------------------------------
A, B, C, D = (0, 10), (0, 0), (10, 0), (10, 10)
E = (5, 5 * math.sqrt(3))
f = Fig(120, 120)
f.segs(A, B, C, D, close=True).seg(E, B).seg(E, C).seg(E, A).seg(E, D)
f.arc(E, A, D, 8, fill='#f6c48f')
L4(f, A, B, C, D).label(E, 'E', dirdeg=-90, dist_px=11)
add(P(f'오른쪽 그림과 같은 정사각형 ABCD에서 {tri("EBC")}가 정삼각형일 때, {ang("AED")}의 크기를 구하시오.',
      None, f.render(),
      ans='150°',
      sol=f'<p>{ov("BA")}={ov("BC")}={ov("BE")}이고 {ang("ABE")}=90°−60°=30°</p>'
          f'<p>{tri("BAE")}에서 {ang("BAE")}={ang("BEA")}=(180°−30°)÷2=75°이므로 {ang("EAD")}=90°−75°=15°</p>'
          f'<p>같은 방법으로 {ang("EDA")}=15°</p><p>∴ {ang("AED")}=180°−(15°+15°)=150°</p>'))

# 28 ------------------------------------------------------------------
A, D, B, C = (1.0, 4.0), (5.0, 4.0), (0.0, 0.0), (6.0, 0.0)
O = inter(A, C, B, D)
f = Fig(130, 95)
f.poly([A, B, C], fill='#f9d7e2', stroke='#222')
f.segs(A, B, C, D, close=True).seg(A, C).seg(B, D)
f.arrow_mark(A, D).arrow_mark(B, C)
L4(f, A, B, C, D).label(O, 'O', dirdeg=90, dist_px=9)
add(P(f'오른쪽 그림과 같이 {ov("AD")}{PAR}{ov("BC")}인 사다리꼴 ABCD에서 두 대각선의 교점을 O라고 하자. {ov("DO")} : {ov("OB")}=2 : 3이고 {tri("DOC")}=18 {CM2}일 때, {tri("ABC")}의 넓이를 구하시오.',
      None, f.render(),
      ans='45 ' + CM2,
      sol=f'<p>{tri("DOC")}와 {tri("BOC")}는 높이가 같으므로 18 : {tri("BOC")}=2 : 3 ∴ {tri("BOC")}=27({CM2})</p>'
          f'<p>{ov("AD")}{PAR}{ov("BC")}이므로 {tri("ABC")}={tri("DBC")} → {tri("ABO")}={tri("DOC")}=18({CM2})</p>'
          f'<p>∴ {tri("ABC")}={tri("ABO")}+{tri("BOC")}=18+27=45({CM2})</p>'))

"""1. 삼각형의 성질"""
import math
from common import *  # noqa: F401,F403

PROBLEMS = []
add = PROBLEMS.append


def _iso(apex_deg, base=2.0):
    """꼭지각이 apex_deg 인 이등변삼각형. A 위, B 왼쪽, C 오른쪽."""
    h = (base / 2) / math.tan(math.radians(apex_deg / 2))
    return (0.0, h), (-base / 2, 0.0), (base / 2, 0.0)


# 01 ------------------------------------------------------------------
A, B, C = (0, 3), (-1.6, 0), (1.6, 0)
D = (0, 0)
f = Fig(120, 110)
f.segs(A, B, C, close=True).seg(A, D).tick(A, B).tick(A, C).right(D, A, C, 5)
f.arc(A, B, D, 13).arc(A, D, C, 13, double=False)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC에서 {ang("A")}의 이등분선과 {ov("BC")}의 교점을 D라고 할 때, 다음 중 옳지 <u>않은</u> 것은?',
      [f'{ang("B")}={ang("C")}', f'{tri("ABD")}{CONG}{tri("ACD")}', f'{ov("AD")}{PERP}{ov("BC")}', f'{ov("BC")}=2{ov("BD")}', f'{ang("ABD")}={ang("BAD")}'],
      f.render(),
      ans='⑤',
      sol=f'<p>이등변삼각형의 꼭지각의 이등분선은 밑변을 수직이등분하므로 ③, ④는 옳고, SAS 합동으로 ②도 옳다. 밑각의 크기는 같으므로 ①도 옳다.</p>'
          f'<p>⑤ {ang("ABD")}={ang("BAD")}이면 {ov("AD")}={ov("BD")}이어야 하는데 항상 성립하지는 않는다.</p>'))

# 02 ------------------------------------------------------------------
B, C = (0, 0), (2, 0)
A = (1, math.tan(math.radians(55)))
Dd = (2.75, 0)
f = Fig(140, 100)
f.segs(A, B, C, close=True).seg(C, Dd).dot(Dd).tick(A, B).tick(A, C)
f.arc(B, C, A, 13, label='55°', lsize=9, ldist=24)
f.arc(A, B, C, 11, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=20)
f.arc(C, A, Dd, 9, fill='#e7b3d6', label='<tspan font-style="italic">y</tspan>', ldist=17)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-100).label(Dd, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC에서 {ang("B")}=55°일 때, {ang(v("x"))}+{ang(v("y"))}의 크기는? (단, 점 D는 {ov("BC")}의 연장선 위의 점이다.)',
      ['185°', '190°', '195°', '200°', '205°'], f.render(),
      ans='③',
      sol=f'<p>{ang("C")}={ang("B")}=55°이므로 {ang(v("x"))}=180°−2×55°=70°</p>'
          f'<p>{ang(v("y"))}=180°−55°=125°</p><p>∴ {ang(v("x"))}+{ang(v("y"))}=70°+125°=195°</p>'))

# 03 ------------------------------------------------------------------
A, B, C = _iso(36, 1.0)
B, C = (0.0, 0.0), (1.0, 0.0)
A = (0.5, 0.5 * math.tan(math.radians(72)))
E = (1.75, 0)
D = polar(1.0, 54, C)
f = Fig(150, 125)
f.segs(A, B, C, close=True).seg(C, E).seg(C, D).seg(B, D).dot(E)
f.tick(A, B).tick(A, C).tick(B, C, 2).tick(C, D, 2)
f.arc(A, B, C, 11, label='36°', lsize=9, ldist=22)
f.arc(C, A, D, 9).arc(C, D, E, 9)
f.arc(D, B, C, 13, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=22)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=20).label(E, 'E', dirdeg=-90)
add(P(f'오른쪽 그림에서 {tri("ABC")}와 {tri("DBC")}는 각각 {ov("AB")}={ov("AC")}, {ov("CB")}={ov("CD")}인 이등변삼각형이다. {ang("ACD")}={ang("DCE")}이고 {ang("A")}=36°일 때, {ang(v("x"))}의 크기는?',
      ['25°', '26°', '27°', '28°', '29°'], f.render(),
      ans='③',
      sol=f'<p>{tri("ABC")}에서 {ang("ACB")}=(180°−36°)÷2=72°이므로 {ang("ACE")}=180°−72°=108°</p>'
          f'<p>{ang("DCE")}=108°÷2=54°</p>'
          f'<p>{tri("CBD")}에서 {ov("CB")}={ov("CD")}이므로 {ang("CBD")}={ang("CDB")}={ang(v("x"))}</p>'
          f'<p>{ang("DCE")}는 {tri("CBD")}의 외각이므로 2{ang(v("x"))}=54° ∴ {ang(v("x"))}=27°</p>'))

# 04 ------------------------------------------------------------------
B, C = (0.0, 0.0), (1.0, 0.0)
A = (0.5, 0.5 * math.tan(math.radians(72)))
D = inter(B, polar(1, 36), A, C)
f = Fig(110, 130)
f.segs(A, B, C, close=True).seg(B, D).tick(A, B).tick(A, C)
f.arc(A, B, C, 13, label='36°', lsize=9, ldist=24)
f.arc(B, C, D, 10).arc(B, D, A, 13)
f.dim(B, C, '8 cm', side=-1, off=10, size=9)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(D, 'D', dirdeg=10)
add(P(f'오른쪽 그림과 같이 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC에서 {ang("B")}의 이등분선이 {ov("AC")}와 만나는 점을 D라고 하자. {ang("A")}=36°, {ov("BC")}=8 cm일 때, {ov("AD")}의 길이를 구하시오.',
      None, f.render(),
      ans='8 cm',
      sol=f'<p>{ang("ABC")}={ang("C")}=(180°−36°)÷2=72°이므로 {ang("ABD")}={ang("DBC")}=36°</p>'
          f'<p>{tri("BCD")}에서 {ang("BDC")}=180°−36°−72°=72°={ang("C")}이므로 {ov("BD")}={ov("BC")}=8 cm</p>'
          f'<p>{tri("ABD")}에서 {ang("A")}={ang("ABD")}=36°이므로 {ov("AD")}={ov("BD")}=8 cm</p>'))

# 05 ------------------------------------------------------------------
Bq, Cq = (0.0, 0.0), (7.0, 0.0)
ax = 62 / 14
h = math.sqrt(49 - ax * ax)
Aq = (ax, h)
L, R = -3.0, ax + 8.7
top_r, bot_r = (R, h), (R, 0.0)
f1, f2 = reflect(top_r, Aq, Cq), reflect(bot_r, Aq, Cq)
f = Fig(175, 150)
f.poly([(L, 0), Cq, Aq, (L, h)], fill='#ffffff', stroke='#222')
f.segs(Aq, top_r, bot_r, Cq, dash=True)
f.poly([Aq, f1, f2, Cq], fill='#f9c9d4', stroke='#222')
f.seg(Aq, Cq).dot((R - 1.2, h))
f.dim(Aq, Cq, '6 cm', side=-1, off=10, size=9)
f.dim(Bq, Cq, '7 cm', side=1, off=-10, size=9)
f.label(Aq, 'A', dirdeg=95).label(Bq, 'B', dirdeg=150).label(Cq, 'C', dirdeg=-10).label((R - 1.2, h), 'D', dirdeg=90)
add(P(f'직사각형 모양의 종이를 다음 그림과 같이 {ov("AC")}를 접는 선으로 하여 접었다. {ov("AC")}=6 cm, {ov("BC")}=7 cm일 때, {tri("ABC")}의 둘레의 길이를 구하시오.',
      None, f.render(), figpos='below',
      ans='20 cm',
      sol=f'<p>{ang("DAC")}={ang("BAC")} (접은 각), {ang("DAC")}={ang("ACB")} (엇각)</p>'
          f'<p>따라서 {ang("BAC")}={ang("BCA")}이므로 {tri("ABC")}는 {ov("BA")}={ov("BC")}인 이등변삼각형이다.</p>'
          f'<p>{ov("AB")}={ov("BC")}=7 cm ∴ (둘레)=7+7+6=20(cm)</p>'))

# 06 ------------------------------------------------------------------
A1, B1, C1 = (0, 2.2), (0, 0), (1.3, 0)
D1, E1, F1 = (2.4, 2.2), (2.4, 0), (3.7, 0)
f = Fig(150, 80)
f.segs(A1, B1, C1, close=True).segs(D1, E1, F1, close=True)
f.right(B1, A1, C1, 5).right(E1, D1, F1, 5)
f.label(A1, 'A', dirdeg=90).label(B1, 'B', dirdeg=225).label(C1, 'C', dirdeg=-45)
f.label(D1, 'D', dirdeg=90).label(E1, 'E', dirdeg=225).label(F1, 'F', dirdeg=-45)
add(P(f'다음 중 오른쪽 그림과 같이 {ang("B")}={ang("E")}=90°인 두 직각삼각형 ABC와 DEF가 합동이 되기 위한 조건이 <u>아닌</u> 것을 모두 고르면? (정답 2개)',
      [f'{ov("BC")}={ov("EF")}, {ov("AC")}={ov("DF")}', f'{ang("A")}={ang("D")}, {ov("AC")}={ov("DF")}', f'{ang("A")}={ang("D")}, {ang("C")}={ang("F")}',
       f'{ang("C")}={ang("F")}, {ov("BC")}={ov("EF")}', f'{ov("AB")}={ov("DE")}, {ov("AC")}={ov("EF")}'],
      f.render(),
      ans='③, ⑤',
      sol=f'<p>① RHS 합동 ② RHA 합동 ④ ASA 합동</p>'
          f'<p>③ 세 각의 크기가 같아도 크기가 다를 수 있으므로 합동이 아니다.</p>'
          f'<p>⑤ {ov("AC")}는 빗변, {ov("EF")}는 빗변이 아닌 변이므로 대응하는 변이 아니다.</p>'))

# 07 ------------------------------------------------------------------
A, B, C = _iso(64, 2.0)
M = (0.0, 0.0)
D, E = foot(M, A, B), foot(M, A, C)
f = Fig(150, 120)
f.segs(A, B, C, close=True).seg(M, D).seg(M, E)
f.right(D, M, B, 5).right(E, M, C, 5).tick(B, M).tick(M, C).tick(D, M, 2).tick(M, E, 2)
f.arc(A, B, C, 11, label='64°', lsize=9, ldist=22)
f.arc(M, B, D, 9, fill='#f6c48f')
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(M, 'M', dirdeg=-90)
f.label(D, 'D', dirdeg=150).label(E, 'E', dirdeg=30)
add(P(f'오른쪽 그림과 같이 {ang("A")}=64°인 {tri("ABC")}에서 {ov("BC")}의 중점을 M이라 하고, 점 M에서 {ov("AB")}, {ov("AC")}에 내린 수선의 발을 각각 D, E라고 하자. {ov("DM")}={ov("EM")}일 때, {ang("BMD")}의 크기는?',
      ['30°', '31°', '32°', '33°', '34°'], f.render(),
      ans='③',
      sol=f'<p>{tri("BDM")}과 {tri("CEM")}에서 {ang("BDM")}={ang("CEM")}=90°, {ov("BM")}={ov("CM")}, {ov("DM")}={ov("EM")}이므로 {tri("BDM")}{CONG}{tri("CEM")} (RHS 합동)</p>'
          f'<p>{ang("B")}={ang("C")}=(180°−64°)÷2=58°</p><p>∴ {ang("BMD")}=90°−58°=32°</p>'))

# 08 ------------------------------------------------------------------
# 빗변 20, 꼭짓점 A 의 이등분선이 BC 와 만나는 D 에서 DC=5 가 되도록 모양을 잡는다.
def _dc(ac):
    bc = math.sqrt(400 - ac * ac)
    return bc * ac / (20 + ac) - 5
ac = solve(_dc, 5.0, 12.0)
bc = math.sqrt(400 - ac * ac)
C = (0.0, 0.0)
A = (0.0, ac)
B = (-bc, 0.0)
D = (-5.0, 0.0)
f = Fig(150, 100)
f.segs(A, B, C, close=True).seg(A, D).right(C, A, B, 5)
f.arc(A, B, D, 16).arc(A, D, C, 19)
f.dim(B, A, '20 cm', side=1, off=12, size=9, curve=True)
f.label(A, 'A', dirdeg=60).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(D, 'D', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("C")}=90°인 직각삼각형 ABC에서 {ang("BAD")}={ang("CAD")}이고 {ov("AB")}=20 cm, {tri("ABD")}의 넓이가 50 {CM2}일 때, {ov("DC")}의 길이를 구하시오.',
      None, f.render(),
      ans='5 cm',
      sol=f'<p>점 D에서 {ov("AB")}에 내린 수선의 발을 E라고 하면 {tri("AED")}{CONG}{tri("ACD")} (RHA 합동)이므로 {ov("DE")}={ov("DC")}</p>'
          f'<p>{tri("ABD")}={fr(1, 2)}×20×{ov("DE")}=50 ∴ {ov("DE")}=5(cm)</p><p>따라서 {ov("DC")}={ov("DE")}=5 cm</p>'))

# 09 ------------------------------------------------------------------
A, B, C, D = (0, 15), (0, 0), (15, 0), (15, 15)
E, F, G = (20, 0), (20, 5), (15, 5)
f = Fig(140, 125)
f.segs(A, B, E, close=False).segs(A, D, C).segs(C, E, F, G).seg(A, E)
f.dim(A, D, '15 cm', side=-1, off=8, size=9)
f.dim(A, E, '25 cm', side=1, off=-9, size=9)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=45)
f.label(E, 'E', dirdeg=-45).label(F, 'F', dirdeg=45).label(G, 'G', dirdeg=160)
add(P(f'오른쪽 그림에서 두 사각형 ABCD와 CEFG는 정사각형이고 세 점 B, C, E는 한 직선 위에 있다. {ov("AD")}=15 cm, {ov("AE")}=25 cm일 때, {ov("CE")}의 길이를 구하시오.',
      None, f.render(),
      ans='5 cm',
      sol=f'<p>{ov("AB")}={ov("AD")}=15 cm이고 {tri("ABE")}는 {ang("B")}=90°인 직각삼각형이므로</p>'
          f'<p>{ov("BE")}<sup>2</sup>=25<sup>2</sup>−15<sup>2</sup>=400 ∴ {ov("BE")}=20(cm)</p>'
          f'<p>{ov("CE")}={ov("BE")}−{ov("BC")}=20−15=5(cm)</p>'))

# 10 ------------------------------------------------------------------
B, C, D, A = (0, 0), (14, 0), (14, 15), (8, 15)
f = Fig(120, 120)
f.poly([A, B, C, D], fill='#d4ecc3', stroke='#222').right(C, B, D, 5).right(D, A, C, 5)
f.dim(A, D, '6 cm', side=-1, off=8, size=9).dim(B, C, '14 cm', side=1, off=-10, size=9)
f.dim(B, A, '17 cm', side=1, off=11, size=9, curve=True)
f.label(A, 'A', dirdeg=110).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(D, 'D', dirdeg=45)
add(P(f'오른쪽 그림과 같이 {ang("C")}={ang("D")}=90°인 사다리꼴 ABCD의 넓이는?',
      ['140 ' + CM2, '145 ' + CM2, '150 ' + CM2, '155 ' + CM2, '160 ' + CM2], f.render(),
      ans='③',
      sol=f'<p>점 A에서 {ov("BC")}에 내린 수선의 발을 H라고 하면 {ov("BH")}=14−6=8(cm)</p>'
          f'<p>{tri("ABH")}에서 {ov("AH")}<sup>2</sup>=17<sup>2</sup>−8<sup>2</sup>=225 ∴ {ov("AH")}=15(cm)</p>'
          f'<p>(넓이)={fr(1, 2)}×(6+14)×15=150({CM2})</p>'))

# 11 ------------------------------------------------------------------
f = Fig(110, 120)
O = (0, 0)
T = (0, 12)
f.seg(T, (-5, 0)).seg(T, (5, 0))
ellipse(f, O, 5, 1.4)
f.seg(O, (5, 0), dash=True).seg(T, O, dash=True).dot(O, 1.4)
f.dim(T, (5, 0), '13 cm', side=1, off=-12, size=9)
f.text((2.5, -2.4), '5 cm', size=9)
add(P(f'오른쪽 그림과 같이 밑면의 반지름의 길이가 5 cm이고, 모선의 길이가 13 cm인 원뿔의 높이와 부피를 차례로 구하시오.',
      None, f.render(),
      ans=f'12 cm, 100π {CM3}',
      sol=f'<p>원뿔의 높이를 <i class="var">h</i> cm라고 하면 <i class="var">h</i><sup>2</sup>=13<sup>2</sup>−5<sup>2</sup>=144 ∴ <i class="var">h</i>=12</p>'
          f'<p>(부피)={fr(1, 3)}×π×5<sup>2</sup>×12=100π({CM3})</p>'))

# 12 ------------------------------------------------------------------
A, B, C, D = (0, 14), (0, 0), (14, 0), (14, 14)
E, F, G, H = (0, 8), (6, 0), (14, 6), (8, 14)
f = Fig(120, 120)
f.poly([A, B, C, D], fill='#e3e6fb', stroke='#222').poly([E, F, G, H], fill='#ffffff', stroke='#222')
f.tick(A, E, 2).tick(B, F, 2).tick(C, G, 2).tick(D, H, 2)
f.right(B, A, C, 5)
f.dim(A, E, '6 cm', side=1, off=18, size=9)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-45).label(D, 'D', dirdeg=45)
f.label(E, 'E', dirdeg=180).label(F, 'F', dirdeg=-90).label(G, 'G', dirdeg=0).label(H, 'H', dirdeg=90)
add(P(f'오른쪽 그림과 같은 정사각형 ABCD에서 {ov("AE")}={ov("BF")}={ov("CG")}={ov("DH")}=6 cm이고 사각형 EFGH의 넓이가 100 {CM2}일 때, 정사각형 ABCD의 넓이를 구하시오.',
      None, f.render(),
      ans='196 ' + CM2,
      sol=f'<p>네 직각삼각형 AEH, BFE, CGF, DHG는 모두 합동(SAS)이므로 사각형 EFGH는 정사각형이고 {ov("EH")}<sup>2</sup>=100</p>'
          f'<p>{ov("AH")}=<i class="var">a</i> cm라고 하면 {tri("AEH")}에서 6<sup>2</sup>+<i class="var">a</i><sup>2</sup>=100, <i class="var">a</i><sup>2</sup>=64 ∴ <i class="var">a</i>=8</p>'
          f'<p>정사각형 ABCD의 한 변의 길이는 6+8=14(cm)이므로 넓이는 14<sup>2</sup>=196({CM2})</p>'))

# 13 ------------------------------------------------------------------
A, B = (0.0, 0.0), (13.0, 0.0)
cx = 50 / 26
C = (cx, math.sqrt(25 - cx * cx))
Fp, Gp = (0, -13), (13, -13)
Ep, Dp = outer_square(A, C, B)
Hp, Ip = outer_square(B, C, A)
f = Fig(150, 160)
f.poly([A, C, B], fill='#f9cfd8', stroke='#222')
f.segs(A, Ep, Dp, C).segs(C, Ip, Hp, B).segs(A, Fp, Gp, B)
f.right(C, A, B, 5)
f.text(centroid(A, Ep, Dp, C), '25 cm²', size=8.5).text(centroid(A, Fp, Gp, B), '169 cm²', size=9)
f.label(A, 'A', dirdeg=200).label(B, 'B', dirdeg=-20).label(C, 'C', dirdeg=80)
f.label(Dp, 'D', dirdeg=100).label(Ep, 'E', dirdeg=180).label(Fp, 'F', dirdeg=225).label(Gp, 'G', dirdeg=-45)
f.label(Hp, 'H', dirdeg=0).label(Ip, 'I', dirdeg=60)
add(P(f'오른쪽 그림은 {ang("C")}=90°인 직각삼각형 ABC의 세 변을 각각 한 변으로 하는 정사각형을 그린 것이다. 두 정사각형 ACDE와 AFGB의 넓이가 각각 25 {CM2}, 169 {CM2}일 때, {tri("ABC")}의 넓이를 구하시오.',
      None, f.render(),
      ans='30 ' + CM2,
      sol=f'<p>{ov("AC")}<sup>2</sup>=25, {ov("AB")}<sup>2</sup>=169이므로 {ov("AC")}=5 cm, {ov("AB")}=13 cm</p>'
          f'<p>{ov("BC")}<sup>2</sup>=169−25=144 ∴ {ov("BC")}=12(cm)</p>'
          f'<p>{tri("ABC")}={fr(1, 2)}×12×5=30({CM2})</p>'))

# 14 ------------------------------------------------------------------
add(P('세 변의 길이가 각각 다음과 같은 삼각형 중 직각삼각형인 것은?',
      ['2 cm, 3 cm, 4 cm', '3 cm, 5 cm, 6 cm', '6 cm, 8 cm, 11 cm', '7 cm, 24 cm, 25 cm', '5 cm, 11 cm, 12 cm'],
      ans='④',
      sol='<p>가장 긴 변의 길이의 제곱이 나머지 두 변의 길이의 제곱의 합과 같은지 확인한다.</p>'
          '<p>① 4²=16, 2²+3²=13 ② 6²=36, 3²+5²=34 ③ 11²=121, 6²+8²=100</p>'
          '<p>④ 25²=625, 7²+24²=49+576=625 ⑤ 12²=144, 5²+11²=146</p><p>따라서 직각삼각형은 ④이다.</p>'))

# 15 ------------------------------------------------------------------
add(P(f'{tri("ABC")}에서 {ov("AB")}={v("c")}, {ov("BC")}={v("a")}, {ov("CA")}={v("b")}일 때, 다음 중 옳지 <u>않은</u> 것은?',
      [f'{v("b")}<sup>2</sup>&gt;{v("a")}<sup>2</sup>+{v("c")}<sup>2</sup>이면 {ang("B")}&gt;90°이다.',
       f'{v("b")}<sup>2</sup>={v("a")}<sup>2</sup>+{v("c")}<sup>2</sup>이면 {ang("B")}=90°이다.',
       f'{v("b")}<sup>2</sup>&lt;{v("a")}<sup>2</sup>+{v("c")}<sup>2</sup>이면 {tri("ABC")}는 예각삼각형이다.',
       f'{ang("B")}&lt;90°이면 {v("b")}<sup>2</sup>&lt;{v("a")}<sup>2</sup>+{v("c")}<sup>2</sup>이다.',
       f'{ang("B")}&gt;90°이면 {tri("ABC")}는 둔각삼각형이다.'],
      ans='③',
      sol=f'<p>③ {v("b")}<sup>2</sup>&lt;{v("a")}<sup>2</sup>+{v("c")}<sup>2</sup>이면 {ang("B")}&lt;90°일 뿐, 다른 각 {ang("A")} 또는 {ang("C")}가 둔각일 수 있으므로 예각삼각형이라고 할 수 없다.</p>'))

# 16 ------------------------------------------------------------------
B, C = (0.0, 0.0), (10.0, 0.0)
A = (6.4, 4.8)
def _semi(p, q, away):
    m = lerp(p, q, 0.5)
    r = dist(p, q) / 2
    a0 = math.degrees(math.atan2(p[1] - m[1], p[0] - m[0]))
    # away 점의 반대쪽으로 볼록하게
    mid1 = polar(r, a0 + 90, m)
    sgn = 1 if dist(mid1, away) > dist(polar(r, a0 - 90, m), away) else -1
    return arcpts(m, r, a0, a0 + 180 * sgn)
f = Fig(150, 100)
f.poly(_semi(B, A, C), fill='#d8ecb0', stroke='#222')
f.poly(_semi(A, C, B), fill='#d8ecb0', stroke='#222')
big = arcpts((5, 0), 5, 0, 180)
f.poly(big, fill='#ffffff', stroke='#222')
f.segs(A, B, C, close=True).right(A, B, C, 5)
f.dim(B, A, '8 cm', side=-1, off=-9, size=9)
f.label(A, 'A', dirdeg=80).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
add(P(f'오른쪽 그림은 {ang("A")}=90°인 직각삼각형 ABC의 세 변을 각각 지름으로 하는 반원을 그린 것이다. {ov("AB")}=8 cm이고 색칠한 부분의 넓이가 24 {CM2}일 때, {ov("BC")}의 길이를 구하시오.',
      None, f.render(),
      ans='10 cm',
      sol=f'<p>(색칠한 부분의 넓이)=(두 작은 반원의 넓이의 합)+{tri("ABC")}−(큰 반원의 넓이)={tri("ABC")}</p>'
          f'<p>({ov("AB")}, {ov("AC")}를 지름으로 하는 반원의 넓이의 합은 {ov("BC")}를 지름으로 하는 반원의 넓이와 같다.)</p>'
          f'<p>{fr(1, 2)}×8×{ov("AC")}=24 ∴ {ov("AC")}=6(cm)</p><p>{ov("BC")}<sup>2</sup>=8<sup>2</sup>+6<sup>2</sup>=100 ∴ {ov("BC")}=10(cm)</p>'))

# 17 ------------------------------------------------------------------
B, C = (0.0, 0.0), (1.0, 0.0)
A = inter(B, polar(1, 70), C, polar(1, 116, C))
I = incenter(A, B, C)
f = Fig(120, 130)
f.segs(A, B, C, close=True).seg(B, I).seg(C, I).dot(I)
f.arc(B, C, I, 14, label='35°', lsize=8.5, ldist=26)
f.arc(C, I, A, 14, label='32°', lsize=8.5, ldist=26)
f.arc(A, B, C, 11, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=20)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(I, 'I', dirdeg=90)
add(P(f'오른쪽 그림에서 점 I는 {tri("ABC")}의 내심이다. {ang("IBC")}=35°, {ang("ICA")}=32°일 때, {ang(v("x"))}의 크기는?',
      ['44°', '45°', '46°', '47°', '48°'], f.render(),
      ans='③',
      sol=f'<p>점 I가 내심이므로 {ang("ABC")}=2×35°=70°, {ang("ACB")}=2×32°=64°</p>'
          f'<p>∴ {ang(v("x"))}=180°−(70°+64°)=46°</p>'))

# 18 ------------------------------------------------------------------
B, C, A = (0.0, 0.0), (10.0, 0.0), (3.6, 4.8)
I = incenter(A, B, C)
r = 2.0
T1 = foot(I, B, C)
T2 = foot(I, A, C)
T3 = foot(I, A, B)
f = Fig(125, 100)
f.segs(A, B, C, close=True).circle(I, r).dot(I, 1.4)
f.seg(I, T1).seg(I, T2).seg(I, T3).right(T1, I, C, 4).right(T2, I, A, 4).right(T3, I, B, 4)
f.dim(I, T1, '2 cm', side=-1, off=-11, size=8.5)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(I, 'I', dirdeg=60, dist_px=7)
add(P(f'오른쪽 그림에서 반지름의 길이가 2 cm인 원 I는 {tri("ABC")}의 내접원이다. {tri("ABC")}의 넓이가 24 {CM2}일 때, {tri("ABC")}의 둘레의 길이는?',
      ['18 cm', '20 cm', '22 cm', '24 cm', '26 cm'], f.render(),
      ans='④',
      sol=f'<p>{tri("ABC")}={fr(1, 2)}×(내접원의 반지름)×(둘레의 길이)이므로</p>'
          f'<p>24={fr(1, 2)}×2×(둘레의 길이) ∴ (둘레의 길이)=24 cm</p>'))

# 19 ------------------------------------------------------------------
A, B, C = tri_sss(12, 8, 10)
I = incenter(A, B, C)
r = dist(I, foot(I, B, C))
D, E, F = foot(I, A, B), foot(I, B, C), foot(I, C, A)
f = Fig(160, 110)
f.segs(A, B, C, close=True).circle(I, r).dot(I, 1.4)
f.dim(B, A, '10 cm', side=-1, off=-11, size=9).dim(C, A, '8 cm', side=1, off=-10, size=9).dim(B, C, '12 cm', side=1, off=-10, size=9)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(I, 'I', dirdeg=-90, dist_px=7)
f.label(D, 'D', dirdeg=130, dist_px=7).label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=40, dist_px=7)
add(P(f'오른쪽 그림과 같이 {tri("ABC")}의 내접원 I와 {ov("AB")}, {ov("BC")}, {ov("CA")}의 접점을 각각 D, E, F라고 하자. {ov("AB")}=10 cm, {ov("BC")}=12 cm, {ov("CA")}=8 cm일 때, {ov("BD")}의 길이를 구하시오.',
      None, f.render(),
      ans='7 cm',
      sol=f'<p>{ov("BD")}={ov("BE")}=<i class="var">x</i> cm라고 하면 {ov("AD")}={ov("AF")}=(10−<i class="var">x</i>) cm, {ov("CE")}={ov("CF")}=(12−<i class="var">x</i>) cm</p>'
          f'<p>{ov("CA")}={ov("AF")}+{ov("CF")}이므로 (10−<i class="var">x</i>)+(12−<i class="var">x</i>)=8</p>'
          f'<p>22−2<i class="var">x</i>=8 ∴ <i class="var">x</i>=7</p>'))

# 20 ------------------------------------------------------------------
O = (0.0, 0.0)
cang = math.degrees(2 * math.asin(7 / 9))
A = polar(9, 115)
C = polar(9, 115 - cang)
B = polar(9, 240)
f = Fig(130, 120)
f.segs(A, B, C, close=True).seg(O, A).seg(O, B).seg(O, C).dot(O, 1.4)
f.dim(A, C, '14 cm', side=-1, off=11, size=9)
f.label(A, 'A', dirdeg=100).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-10).label(O, 'O', dirdeg=-80, dist_px=9)
add(P(f'오른쪽 그림에서 점 O는 {tri("ABC")}의 외심이다. {ov("AC")}=14 cm이고 {tri("AOC")}의 둘레의 길이가 32 cm일 때, {tri("ABC")}의 외접원의 반지름의 길이를 구하시오.',
      None, f.render(),
      ans='9 cm',
      sol=f'<p>점 O가 외심이므로 {ov("OA")}={ov("OC")}(외접원의 반지름)</p>'
          f'<p>{ov("OA")}+{ov("OC")}=32−14=18(cm) ∴ {ov("OA")}=9 cm</p>'
          f'<p>따라서 외접원의 반지름의 길이는 9 cm이다.</p>'))

# 21 ------------------------------------------------------------------
B, C = (0.0, 0.0), (16.0, 0.0)
A = polar(8, 60)
M = (8.0, 0.0)
f = Fig(150, 85)
f.segs(A, B, C, close=True).seg(A, M).dot(M, 1.4)
f.right(A, B, C, 5).arc(B, C, A, 10, label='60°', lsize=8.5, ldist=21)
f.tick(B, M, 2).tick(M, C, 2)
f.dim(B, C, '16 cm', side=1, off=-13, size=9, curve=True)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20).label(M, 'M', dirdeg=-90, dist_px=8)
add(P(f'오른쪽 그림과 같이 {ang("A")}=90°인 직각삼각형 ABC에서 {ov("BC")}의 중점을 M이라고 할 때, {tri("ABM")}의 둘레의 길이는?',
      ['20 cm', '22 cm', '24 cm', '26 cm', '28 cm'], f.render(),
      ans='③',
      sol=f'<p>직각삼각형의 빗변의 중점은 외심이므로 {ov("AM")}={ov("BM")}={ov("CM")}=8 cm</p>'
          f'<p>{tri("ABM")}은 {ov("MA")}={ov("MB")}이고 {ang("B")}=60°이므로 정삼각형이다. ∴ {ov("AB")}=8 cm</p>'
          f'<p>(둘레의 길이)=8×3=24(cm)</p>'))

# 22 ------------------------------------------------------------------
O = (0.0, 0.0)
B = polar(1, -82)
A = polar(1, -122)
C = polar(1, -12)
f = Fig(140, 90)
f.segs(A, B, C, close=True).seg(O, A).seg(O, B).seg(O, C).dot(O, 1.4)
f.arc(O, A, B, 12, label='40°', lsize=8.5, ldist=23)
f.arc(O, B, C, 12, label='70°', lsize=8.5, ldist=23)
f.arc(B, A, C, 8, fill='#f6c48f')
f.label(A, 'A', dirdeg=200).label(B, 'B', dirdeg=-90).label(C, 'C', dirdeg=0).label(O, 'O', dirdeg=90)
add(P(f'오른쪽 그림에서 점 O는 {tri("ABC")}의 외심이다. {ang("AOB")}=40°, {ang("BOC")}=70°일 때, {ang("ABC")}의 크기는?',
      ['115°', '120°', '125°', '130°', '135°'], f.render(),
      ans='③',
      sol=f'<p>{ov("OA")}={ov("OB")}={ov("OC")}이므로</p>'
          f'<p>{tri("OAB")}에서 {ang("OBA")}=(180°−40°)÷2=70°</p>'
          f'<p>{tri("OBC")}에서 {ang("OBC")}=(180°−70°)÷2=55°</p>'
          f'<p>∴ {ang("ABC")}=70°+55°=125°</p>'))

# 23 ------------------------------------------------------------------
add(P('다음 중 옳지 <u>않은</u> 것은?',
      ['삼각형의 외심에서 세 꼭짓점에 이르는 거리는 같다.', '둔각삼각형의 외심은 삼각형의 외부에 있다.',
       '삼각형의 내심은 세 내각의 이등분선의 교점이다.', '직각삼각형의 외심은 빗변의 중점이다.',
       '삼각형의 외심은 항상 삼각형의 내부에 있다.'],
      ans='⑤',
      sol='<p>⑤ 외심은 예각삼각형이면 내부, 직각삼각형이면 빗변의 중점, 둔각삼각형이면 외부에 있다. 따라서 항상 내부에 있는 것은 아니다.</p>'))

# 24 ------------------------------------------------------------------
A, B, C = _iso(72, 2.0)
I = incenter(A, B, C)
O = circumcenter(A, B, C)
f = Fig(140, 135)
f.segs(A, B, C, close=True).seg(B, I).seg(C, I).seg(B, O).seg(C, O).dot(I, 1.6).dot(O, 1.6)
f.tick(A, B).tick(A, C)
f.arc(A, B, C, 12, label='72°', lsize=8.5, ldist=23)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
f.label(I, 'I', dirdeg=90, dist_px=8).label(O, 'O', dirdeg=-90, dist_px=8)
add(P(f'오른쪽 그림에서 두 점 I, O는 각각 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC의 내심과 외심이다. {ang("A")}=72°일 때, {ang("BOC")}−{ang("BIC")}의 크기를 구하시오.',
      None, f.render(),
      ans='18°',
      sol=f'<p>점 O가 외심이므로 {ang("BOC")}=2{ang("A")}=144°</p>'
          f'<p>점 I가 내심이므로 {ang("BIC")}=90°+{fr(1, 2)}{ang("A")}=90°+36°=126°</p>'
          f'<p>∴ {ang("BOC")}−{ang("BIC")}=144°−126°=18°</p>'))

# 25 예제 1 ------------------------------------------------------------
xdeg = 25
B = (0.0, 0.0)
A = polar(1, xdeg)
C = (2 * math.cos(math.radians(xdeg)), 0.0)
t = solve(lambda t: dist(polar(t, xdeg), C) - 1, 1.2, 4)
D = polar(t, xdeg)
E = (2 * D[0] - C[0], 0.0)
F = polar(t + 0.5, xdeg)
f = Fig(170, 90)
f.seg(B, F).seg(B, (E[0] + 0.35, 0)).segs(A, C, D, E).dot(F)
f.tick(B, A).tick(A, C).tick(C, D).tick(D, E)
f.arc(B, C, A, 16, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=27)
f.arc(D, F, E, 9, label='100°', lsize=8.5, ldist=21)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=180).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=135)
f.label(E, 'E', dirdeg=-90).label(F, 'F', dirdeg=90)
add(P(f'다음 그림에서 {ov("AB")}={ov("AC")}={ov("CD")}={ov("DE")}이고 {ang("FDE")}=100°일 때, {ang(v("x"))}의 크기를 구하시오.',
      None, f.render(), figpos='below', tag='예제',
      ans='25°',
      sol=f'<p>{tri("ABC")}에서 {ang("ACB")}={ang("B")}={ang(v("x"))}이므로 {ang("DAC")}=2{ang(v("x"))}</p>'
          f'<p>{tri("CAD")}에서 {ang("CDA")}={ang("CAD")}=2{ang(v("x"))}</p>'
          f'<p>{tri("DBC")}에서 {ang("DCE")}={ang(v("x"))}+2{ang(v("x"))}=3{ang(v("x"))}이므로 {ang("DEC")}=3{ang(v("x"))}</p>'
          f'<p>{tri("DBE")}에서 {ang("FDE")}={ang(v("x"))}+3{ang(v("x"))}=4{ang(v("x"))}=100° ∴ {ang(v("x"))}=25°</p>'))

# 26 유제 1 ------------------------------------------------------------
bdeg = 24
B = (0.0, 0.0)
D = polar(1, bdeg)
E = (2 * math.cos(math.radians(bdeg)), 0.0)
t = solve(lambda t: dist(polar(t, bdeg), E) - 1, 1.2, 4)
A = polar(t, bdeg)
Fe = foot(A, B, (5, 0))
C = (2 * Fe[0] - E[0], 0.0)
f = Fig(170, 90)
f.seg(B, polar(t + 0.6, bdeg)).seg(B, (C[0] + 0.3, 0)).segs(D, E, A, C)
f.tick(B, D).tick(D, E).tick(E, A).tick(A, C)
f.arc(B, E, D, 16, label='24°', lsize=8.5, ldist=29)
f.arc(A, E, C, 9, fill='#f6c48f')
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=180).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=120).label(E, 'E', dirdeg=-90)
add(P(f'다음 그림에서 {ov("BD")}={ov("DE")}={ov("EA")}={ov("AC")}이고 {ang("B")}=24°일 때, {ang("EAC")}의 크기를 구하시오.',
      None, f.render(), figpos='below', tag='유제',
      ans='36°',
      sol=f'<p>{tri("DBE")}에서 {ang("DEB")}={ang("B")}=24°이므로 {ang("ADE")}=24°+24°=48°</p>'
          f'<p>{tri("EDA")}에서 {ang("EAD")}={ang("EDA")}=48°</p>'
          f'<p>{tri("ABE")}에서 {ang("AEC")}={ang("B")}+{ang("BAE")}=24°+48°=72°</p>'
          f'<p>{tri("EAC")}에서 {ang("ACE")}={ang("AEC")}=72°이므로 {ang("EAC")}=180°−2×72°=36°</p>'))

# 27 예제 2 ------------------------------------------------------------
def _io_fig(adeg):
    A, B, C = _iso(adeg, 2.0)
    I = incenter(A, B, C)
    O = circumcenter(A, B, C)
    f = Fig(110, 125)
    f.segs(A, B, C, close=True).seg(B, O).seg(C, O).seg(B, I).seg(C, I).dot(I, 1.4).dot(O, 1.4)
    f.tick(A, B).tick(A, C)
    f.arc(A, B, C, 11, label=f'{adeg}°', lsize=8.5, ldist=22)
    f.arc(B, I, O, 18, fill='#f6c48f')
    f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-20)
    f.label(I, 'I', dirdeg=-90, dist_px=8).label(O, 'O', dirdeg=90, dist_px=8)
    return f.render()

add(P(f'오른쪽 그림과 같이 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC에서 점 I는 내심이고, 점 O는 외심이다. {ang("A")}=36°일 때, {ang("OBI")}의 크기를 구하시오.',
      None, _io_fig(36), tag='예제',
      ans='18°',
      sol=f'<p>{ang("BOC")}=2{ang("A")}=72°이므로 {tri("OBC")}에서 {ang("OBC")}=(180°−72°)÷2=54°</p>'
          f'<p>{ang("ABC")}=(180°−36°)÷2=72°이므로 {ang("IBC")}={fr(1, 2)}×72°=36°</p>'
          f'<p>∴ {ang("OBI")}={ang("OBC")}−{ang("IBC")}=54°−36°=18°</p>'))

# 28 유제 2 ------------------------------------------------------------
add(P(f'오른쪽 그림에서 두 점 I, O는 각각 {ov("AB")}={ov("AC")}인 이등변삼각형 ABC의 내심과 외심이다. {ang("A")}=48°일 때, {ang("OBI")}의 크기를 구하시오.',
      None, _io_fig(48), tag='유제',
      ans='9°',
      sol=f'<p>{ang("BOC")}=2{ang("A")}=96°이므로 {tri("OBC")}에서 {ang("OBC")}=(180°−96°)÷2=42°</p>'
          f'<p>{ang("ABC")}=(180°−48°)÷2=66°이므로 {ang("IBC")}={fr(1, 2)}×66°=33°</p>'
          f'<p>∴ {ang("OBI")}={ang("OBC")}−{ang("IBC")}=42°−33°=9°</p>'))

# 29 ------------------------------------------------------------------
A, B, C = _iso(44, 2.0)
D = lerp(A, B, 0.5)
E = inter(D, (D[0] + (A[1] - B[1]), D[1] - (A[0] - B[0])), A, C)
f = Fig(100, 140)
f.poly([D, B, E], fill='#d4ecc3', stroke='#222')
f.seg(A, D, dash=True).seg(A, E, dash=True).seg(B, C).seg(E, C)
f.arc(A, B, C, 11, fill='#f6c48f', label='<tspan font-style="italic">x</tspan>', ldist=20)
f.arc(B, C, E, 16, label='24°', lsize=8.5, ldist=27)
f.label(A, 'A', dirdeg=90).label(B, 'B', dirdeg=210).label(C, 'C', dirdeg=-30).label(D, 'D', dirdeg=170).label(E, 'E', dirdeg=10)
add(P(f'오른쪽 그림은 {ov("AB")}={ov("AC")}인 이등변삼각형 모양의 종이를 {ov("DE")}를 접는 선으로 하여 꼭짓점 A가 꼭짓점 B에 오도록 접은 것이다. {ang("EBC")}=24°일 때, {ang(v("x"))}의 크기를 구하시오.',
      None, f.render(),
      ans='44°',
      sol=f'<p>접은 각이므로 {ang("EBD")}={ang("A")}={ang(v("x"))}</p>'
          f'<p>{ang("ABC")}={ang(v("x"))}+24°이고 {ang("ACB")}={ang("ABC")}이므로</p>'
          f'<p>{ang(v("x"))}+2({ang(v("x"))}+24°)=180°, 3{ang(v("x"))}=132° ∴ {ang(v("x"))}=44°</p>'))

# 30 ------------------------------------------------------------------
B, C, A = (0.0, 0.0), (15.0, 0.0), (15.0, 8.0)
D = lerp(A, B, 8 / 17)
E = inter(D, (D[0] + (A[1] - B[1]), D[1] - (A[0] - B[0])), B, C)
f = Fig(150, 100)
f.segs(A, B, C, close=True).seg(D, E).right(C, A, B, 5).right(D, B, E, 5)
f.tick(A, C, 2).tick(A, D, 2)
f.dim(B, A, '17 cm', side=1, off=9, size=9, curve=True, t=0.5).dim(A, C, '8 cm', side=1, off=-13, size=9, t=0.62).dim(B, C, '15 cm', side=1, off=-11, size=9, t=0.35)
f.label(A, 'A', dirdeg=60).label(B, 'B', dirdeg=200).label(C, 'C', dirdeg=-30).label(D, 'D', dirdeg=-150, dist_px=10).label(E, 'E', dirdeg=-90)
add(P(f'오른쪽 그림과 같이 {ang("C")}=90°인 직각삼각형 ABC에서 {ov("AC")}={ov("AD")}이고, {ov("AB")}{PERP}{ov("DE")}이다. {ov("AB")}=17 cm, {ov("BC")}=15 cm, {ov("AC")}=8 cm일 때, {tri("BED")}의 둘레의 길이를 구하시오.',
      None, f.render(),
      ans='24 cm',
      sol=f'<p>{tri("ADE")}와 {tri("ACE")}에서 {ang("ADE")}={ang("ACE")}=90°, {ov("AE")}는 공통, {ov("AD")}={ov("AC")}이므로 {tri("ADE")}{CONG}{tri("ACE")} (RHS 합동) ∴ {ov("DE")}={ov("CE")}</p>'
          f'<p>{ov("BD")}=17−8=9(cm)</p>'
          f'<p>(둘레)={ov("BD")}+{ov("DE")}+{ov("EB")}=9+({ov("CE")}+{ov("EB")})=9+15=24(cm)</p>'))

# 31 ------------------------------------------------------------------
B, A, C, D, E = (0, 0), (0, 3), (6, 0), (9, 0), (9, 6)
f = Fig(150, 90)
f.poly([A, C, E], fill='#fbf1b7', stroke='#222')
f.segs(A, B, D, E).right(B, A, C, 5).right(D, C, E, 5)
f.dim(B, C, '6 cm', side=1, off=-10, size=9).dim(C, D, '3 cm', side=1, off=-10, size=9)
f.label(A, 'A', dirdeg=135).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-90).label(D, 'D', dirdeg=-45).label(E, 'E', dirdeg=45)
add(P(f'오른쪽 그림에서 {tri("ABC")}{CONG}{tri("CDE")}이고, 세 점 B, C, D는 한 직선 위에 있다. {ov("BC")}=6 cm, {ov("CD")}=3 cm일 때, 다음을 구하시오.'
      f'<div class="sub">(1) {tri("ACE")}는 어떤 삼각형인지 말하시오.</div><div class="sub">(2) {tri("ACE")}의 넓이</div>',
      None, f.render(),
      ans=f'(1) {ang("ACE")}=90°인 직각이등변삼각형 (2) {fr(45, 2)} {CM2}',
      sol=f'<p>(1) {ov("AC")}={ov("CE")}이고 {ang("ACB")}+{ang("ECD")}={ang("ACB")}+{ang("CAB")}=90°이므로 {ang("ACE")}=180°−90°=90°</p>'
          f'<p>따라서 {tri("ACE")}는 {ang("ACE")}=90°인 직각이등변삼각형이다.</p>'
          f'<p>(2) {ov("AB")}={ov("CD")}=3 cm이므로 {ov("AC")}<sup>2</sup>=6<sup>2</sup>+3<sup>2</sup>=45</p>'
          f'<p>{tri("ACE")}={fr(1, 2)}×{ov("AC")}×{ov("CE")}={fr(1, 2)}×45={fr(45, 2)}({CM2})</p>'))

# 32 ------------------------------------------------------------------
B, A, C = (0.0, 0.0), (0.0, 12.0), (16.0, 0.0)
O = (8.0, 6.0)
I = (4.0, 4.0)
f = Fig(130, 120)
f.circle(O, 10).segs(A, B, C, close=True).circle(I, 4).dot(I, 1.4).dot(O, 1.4).right(B, A, C, 5)
f.dim(B, A, '12 cm', side=1, off=21, size=9).dim(B, C, '16 cm', side=1, off=-11, size=9)
f.label(A, 'A', dirdeg=120).label(B, 'B', dirdeg=225).label(C, 'C', dirdeg=-20).label(I, 'I', dirdeg=0, dist_px=7).label(O, 'O', dirdeg=60, dist_px=8)
add(P(f'오른쪽 그림에서 두 점 I, O는 각각 {ang("B")}=90°인 직각삼각형 ABC의 내심과 외심이다. {ov("AB")}=12 cm, {ov("BC")}=16 cm일 때, {tri("ABC")}의 외접원과 내접원의 둘레의 길이의 차를 구하시오.',
      None, f.render(),
      ans='12π cm',
      sol=f'<p>{ov("AC")}<sup>2</sup>=12<sup>2</sup>+16<sup>2</sup>=400 ∴ {ov("AC")}=20 cm</p>'
          f'<p>외접원의 반지름은 빗변의 길이의 절반이므로 10 cm</p>'
          f'<p>내접원의 반지름을 <i class="var">r</i> cm라고 하면 {fr(1, 2)}×<i class="var">r</i>×(12+16+20)={fr(1, 2)}×12×16, 24<i class="var">r</i>=96 ∴ <i class="var">r</i>=4</p>'
          f'<p>(둘레의 차)=2π×10−2π×4=12π(cm)</p>'))

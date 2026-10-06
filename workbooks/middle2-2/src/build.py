"""중2-2 유형 문제지 HTML 생성기.

python3 build.py  →  ../output/workbook.html  (render.mjs 로 PDF 변환)
"""
import html
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))

import u1, u2, u3, u4, u5, u6  # noqa: E402

UNITS = [
    ('1', '삼각형의 성질', u1.PROBLEMS),
    ('2', '사각형의 성질', u2.PROBLEMS),
    ('3', '도형의 닮음', u3.PROBLEMS),
    ('4', '평행선 사이의 선분의 길이의 비', u4.PROBLEMS),
    ('5', '경우의 수', u5.PROBLEMS),
    ('6', '확률', u6.PROBLEMS),
]

TITLE = '중학 수학 2-2 단원별 유형 문제'

CSS = r"""
@page { size: A4; margin: 11mm 11mm 12mm 11mm;
  @bottom-center { content: counter(page); font-size: 9pt; color: #555; font-family: 'Noto Sans CJK KR', sans-serif; } }
* { box-sizing: border-box; }
html, body { margin: 0; padding: 0; }
body { font-family: 'Noto Sans CJK KR', 'NanumGothic', sans-serif; font-size: 10pt; line-height: 1.62; color: #1a1a1a; }
i.var { font-family: 'Liberation Serif', 'Times New Roman', serif; font-style: italic; font-size: 1.12em; }
.ov { text-decoration: overline; text-decoration-thickness: 0.8px; text-underline-offset: 0; font-family: 'Liberation Serif', 'Times New Roman', serif; font-size: 1.08em; }
.fr { display: inline-flex; flex-direction: column; vertical-align: middle; text-align: center; font-size: 0.86em; line-height: 1.12; margin: 0 1px; }
.fr > span:first-child { border-bottom: 0.8px solid #1a1a1a; padding: 0 2px; }
.fr > span:last-child { padding: 0 2px; }
sup { font-size: 0.7em; line-height: 0; }
.par { letter-spacing: -0.12em; font-style: italic; margin: 0 1px; }

.page { height: 274mm; page-break-after: always; break-after: page; position: relative; display: flex; flex-direction: column; }
.page:last-child { page-break-after: auto; break-after: auto; }
.phead { display: flex; align-items: flex-end; justify-content: space-between; border-bottom: 2.2px solid #1d7f8c; padding-bottom: 3px; margin-bottom: 0; }
.phead .unit { font-size: 13pt; font-weight: 700; color: #135e68; }
.phead .unit b { display: inline-block; background: #1d7f8c; color: #fff; border-radius: 3px; padding: 0 7px; margin-right: 7px; font-size: 11pt; }
.phead .kind { font-size: 9pt; color: #555; }
.phead .kind b { color: #1d7f8c; }

.grid { flex: 1; display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; grid-auto-flow: column; }
.cell { padding: 9px 11px 6px 11px; overflow: hidden; position: relative; }
.cell:nth-child(1), .cell:nth-child(2) { border-right: 1px solid #9bbcc1; }
.cell:nth-child(1), .cell:nth-child(3) { border-bottom: 1px solid #9bbcc1; }
.qnum { font-size: 17pt; font-weight: 800; color: #1d7f8c; line-height: 1; margin-right: 6px; float: left; font-family: 'NanumSquare', 'Noto Sans CJK KR', sans-serif; }
.qtag { display: inline-block; font-size: 7.5pt; color: #fff; background: #7a5ea8; border-radius: 3px; padding: 0 5px; margin-left: 2px; vertical-align: 3px; line-height: 1.5; }
.qtext { overflow: hidden; }
.qtext p { margin: 0 0 2px 0; }
.fig-right { float: right; margin: 0 0 4px 8px; }
.fig-below { text-align: center; margin: 6px 0 4px 0; clear: both; }
.choices { display: grid; column-gap: 8px; row-gap: 1px; margin-top: 7px; clear: both; }
.choices.c1 { grid-template-columns: 1fr; }
.choices.c2 { grid-template-columns: 1fr 1fr; }
.choices.c3 { grid-template-columns: 1fr 1fr 1fr; }
.choices.c3 > div, .choices.c5 > div { white-space: nowrap; }
.choices.c5 { display: flex; flex-wrap: wrap; column-gap: 20px; }
.choices.frac { column-gap: 26px; row-gap: 6px; }
.choices.c3, .choices.c2 { row-gap: 4px; }
.box { border: 1px solid #aaa; border-radius: 6px; padding: 4px 9px; margin: 6px 0; clear: both; }
.box .bt { font-size: 8.5pt; color: #555; }
.sub { margin: 4px 0 0 0; }
table.cal { border-collapse: collapse; font-size: 8.5pt; margin: 2px auto; }
table.cal td, table.cal th { border: 1px solid #999; width: 20px; text-align: center; padding: 0 1px; line-height: 1.45; }
table.cal th { background: #e7f2f3; }
table.cal .sun { color: #c0392b; } table.cal .sat { color: #2160b5; }

/* 해설 */
.sol-unit { break-before: page; page-break-before: always; }
.sol-head { display: flex; align-items: flex-end; justify-content: space-between; border-bottom: 2.2px solid #b5651d; padding-bottom: 3px; margin-bottom: 8px; }
.sol-head .unit { font-size: 13pt; font-weight: 700; color: #8a4a12; }
.sol-head .unit b { display: inline-block; background: #b5651d; color: #fff; border-radius: 3px; padding: 0 7px; margin-right: 7px; font-size: 11pt; }
.sol-head .kind { font-size: 9pt; color: #555; }
.sols { column-count: 2; column-gap: 9mm; column-rule: 1px solid #d9c3ad; font-size: 9.2pt; line-height: 1.6; }
.sol { break-inside: avoid; margin-bottom: 9px; }
.sol .sn { font-weight: 800; color: #b5651d; font-size: 11pt; margin-right: 6px; font-family: 'NanumSquare', 'Noto Sans CJK KR', sans-serif; }
.sol .sa { display: inline-block; border: 1px solid #b5651d; color: #8a4a12; border-radius: 3px; padding: 0 5px; font-weight: 700; font-size: 8.8pt; line-height: 1.5; }
.sol .sb { margin-top: 2px; }
.sol .sb p { margin: 0 0 1px 0; }

/* 정답 */
.ans-page { break-before: page; page-break-before: always; }
.ans-head { border-bottom: 2.2px solid #2d6a3e; padding-bottom: 3px; margin-bottom: 8px; font-size: 13pt; font-weight: 700; color: #2d6a3e; display: flex; justify-content: space-between; align-items: flex-end; }
.ans-head .kind { font-size: 9pt; color: #555; font-weight: 400; }
.ans-unit { margin-bottom: 9px; break-inside: avoid; }
.ans-unit h3 { font-size: 10.5pt; margin: 0 0 3px 0; color: #2d6a3e; }
table.ans { border-collapse: collapse; width: 100%; table-layout: fixed; font-size: 8.8pt; }
table.ans td { border: 1px solid #b7cdb9; padding: 2px 4px; vertical-align: middle; line-height: 1.45; }
table.ans td.n { width: 6.5%; background: #edf5ee; text-align: center; font-weight: 700; color: #2d6a3e; }
"""


def plain_len(s):
    s = re.sub(r'<span class="fr"><span>(.*?)</span><span>(.*?)</span></span>', r'\1/\2', s)
    s = re.sub(r'<[^>]+>', '', s)
    return len(html.unescape(s))


def render_choices(ch):
    if not ch:
        return ''
    m = max(plain_len(c) for c in ch)
    has_frac = any('class="fr"' in c for c in ch)
    if m <= 6:
        cls = 'c5 frac' if has_frac else 'c5'
    elif m <= 8:
        cls = 'c3'
    elif m <= 16:
        cls = 'c2'
    else:
        cls = 'c1'
    items = ''.join(f'<div>{"①②③④⑤"[i]} {c}</div>' for i, c in enumerate(ch))
    return f'<div class="choices {cls}">{items}</div>'


def render_cell(no, p):
    tag = f'<span class="qtag">{p["tag"]}</span>' if p.get('tag') else ''
    fig = ''
    figb = ''
    if p.get('fig'):
        if p['figpos'] == 'right':
            fig = f'<div class="fig-right">{p["fig"]}</div>'
        else:
            figb = f'<div class="fig-below">{p["fig"]}</div>'
    return (f'<div class="cell"><div class="qnum">{no:02d}</div>'
            f'<div class="qtext">{fig}{tag} {p["q"]}{figb}{render_choices(p.get("ch"))}</div></div>')


def unit_head(code, name, kind, cls='phead'):
    return (f'<div class="{cls}"><div class="unit"><b>{code}</b>{name}</div>'
            f'<div class="kind">{TITLE} · <b>{kind}</b></div></div>')


def build():
    parts = []
    # 문제
    for code, name, probs in UNITS:
        for i in range(0, len(probs), 4):
            cells = ''.join(render_cell(i + j + 1, probs[i + j]) for j in range(min(4, len(probs) - i)))
            # 빈 칸 채우기(격자 유지)
            cells += ''.join('<div class="cell"></div>' for _ in range(4 - min(4, len(probs) - i)))
            parts.append(f'<section class="page">{unit_head(code, name, "문제")}<div class="grid">{cells}</div></section>')
    # 해설
    for code, name, probs in UNITS:
        items = []
        for i, p in enumerate(probs):
            tag = f' <span style="font-size:8pt;color:#7a5ea8">[{p["tag"]}]</span>' if p.get('tag') else ''
            items.append(f'<div class="sol"><span class="sn">{i + 1:02d}</span><span class="sa">정답 {p["ans"]}</span>{tag}'
                         f'<div class="sb">{p["sol"]}</div></div>')
        parts.append(f'<section class="sol-unit">{unit_head(code, name, "해설", "sol-head")}<div class="sols">{"".join(items)}</div></section>')
    # 정답
    blocks = []
    for code, name, probs in UNITS:
        rows = []
        per = 5
        for i in range(0, len(probs), per):
            tds = ''
            for j in range(per):
                if i + j < len(probs):
                    tds += f'<td class="n">{i + j + 1:02d}</td><td>{probs[i + j]["ans"]}</td>'
                else:
                    tds += '<td class="n"></td><td></td>'
            rows.append(f'<tr>{tds}</tr>')
        blocks.append(f'<div class="ans-unit"><h3>{code}. {name}</h3><table class="ans">{"".join(rows)}</table></div>')
    parts.append(f'<section class="ans-page"><div class="ans-head"><div>빠른 정답</div><div class="kind">{TITLE} · 정답</div></div>{"".join(blocks)}</section>')

    doc = (f'<!doctype html><html lang="ko"><head><meta charset="utf-8"><title>{TITLE}</title>'
           f'<style>{CSS}</style></head><body>{"".join(parts)}</body></html>')
    out = os.path.join(os.path.dirname(__file__), '..', 'output', 'workbook.html')
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, 'w', encoding='utf-8') as f:
        f.write(doc)
    print('wrote', os.path.abspath(out), sum(len(u[2]) for u in UNITS), 'problems')


if __name__ == '__main__':
    build()

# vector

2022-09-05 

矢量数据编辑 可视化

包含SVG格式, GCode格式等

# SVG 格式

https://www.svgviewer.dev/

https://developer.mozilla.org/zh-CN/docs/Web/SVG

https://developer.mozilla.org/zh-CN/docs/Web/SVG/Tutorial

- 如果 HTML 是 XHTML 并且声明类型为`application/xhtml+xml`，可以直接把 SVG 嵌入到 XML 源码中。
- 否则 `<object data="image.svg" type="image/svg+xml" />`
- 其他 `<iframe src="image.svg"></iframe>`

```html
<p>
路径解析
This is where the hard-to-parse paths are handled.
Uppercase rules are absolute positions, lowercase are relative.
Types of path rules:
<p/>
<p>
<ol>
<li>M/m - (x y)+ - Move to (without drawing)
<li>Z/z - (no params) - Close path (back to starting point)
<li>L/l - (x y)+ - Line to
<li>H/h - x+ - Horizontal ine to
<li>V/v - y+ - Vertical line to
<li>C/c - (mX1 y1 x2 y2 x y)+ - Cubic bezier to
<li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the mX1, y1 of this bezier)
<li>Q/q - (mX1 y1 x y)+ - Quadratic bezier to
<li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
</ol>
<p/>
Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
<p>
例如: M14,85l3,9h72c0,0,5-9,4-10c-2-2-79,0-79,1
<p/>
```

# GCode格式

https://ncviewer.com/

https://reprap.org/wiki/G-code
https://reprap.org/wiki/G-code/zh_cn

https://en.wikipedia.org/wiki/G-code

https://duet3d.dozuki.com/Wiki/Gcode

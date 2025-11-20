package com.example.handbook

import android.R.attr.text
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class NotePageFragment : Fragment() {

    private var pageId: Long = 0L

    companion object {
        fun newInstance(pageId: Long): NotePageFragment {
            val frag = NotePageFragment()
            val args = Bundle()
            args.putLong("page_id", pageId)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageId = arguments?.getLong("page_id") ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.item_note_page, container, false)
        val web = view.findViewById<WebView>(R.id.noteWebView)
        val settings = web.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<title>Advanced Note Editor</title>
<style>
  :root{
    --blue:#1565c0;
    --blue-100:#e3f2fd;
    --btn-pad:6px 10px;
    --btn-radius:8px;
    --shadow:0 2px 6px rgba(0,0,0,.08);
  }
  *{box-sizing:border-box}
  html,body{height:100%}
  body{
    margin:0;
    font-family:system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif;
    background:#f4f6f8;color:#333;
    display:flex;flex-direction:column;min-height:100%;
  }
  header, footer{
    position:sticky;
    left:0;right:0;
    background:#1976d2;color:#fff;
    display:flex;flex-wrap:wrap;gap:8px;
    padding:8px;align-items:center;justify-content:center;
    z-index:10;
  }
  header{top:0}
  footer{bottom:0}
  .toolbar{
    display:flex;flex-wrap:wrap;gap:8px;align-items:center;justify-content:center;
    max-width:100%;overflow-x:auto;scrollbar-width:none;
  }
  .btn, select{
    appearance:none;border:none;background:#fff;color:var(--blue);
    padding:var(--btn-pad);border-radius:var(--btn-radius);
    cursor:pointer;font-size:14px;box-shadow:var(--shadow);transition:.15s;
    user-select:none;
  }
  .btn:hover, select:hover{background:var(--blue-100)}
  .btn.active{background:var(--blue);color:#fff;font-weight:700}
  .btn.small{padding:4px 8px}
  #editor{
    flex:1;background:#fff;margin:10px;border-radius:10px;padding:16px;
    overflow-y:auto;box-shadow:var(--shadow);font-size:16px;line-height:1.5;
  }
  #editor:focus{outline:none}
  table{border-collapse:collapse;width:100%;margin-top:10px}
  table,th,td{border:1px solid #ccc}
  th{font-weight:700;background:#e3f2fd}
  td,th{padding:8px}

  /* Checklist (real checkboxes; click only on the box toggles) */
  ul.checklist{list-style:none;padding-left:24px;margin:6px 0}
  ul.checklist li{position:relative;margin:4px 0}
  ul.checklist input[type="checkbox"]{
    position:absolute;left:-24px;top:.3em;transform:scale(1.1);
  }

  /* Color palette popovers */
  .color-wrap{position:relative;display:inline-block}
  .color-toggle{display:inline-flex;gap:6px;align-items:center}
  .swatch{
    width:16px;height:16px;border-radius:4px;border:1px solid rgba(0,0,0,.15);
    display:inline-block;box-shadow:var(--shadow);
  }
  .popover{
    position:absolute;top:110%;left:0;background:#fff;border-radius:10px;
    box-shadow:0 8px 24px rgba(0,0,0,.16);padding:10px;z-index:1000;
    display:none;min-width:210px;
  }
  .popover.open{display:block}
  .grid{display:grid;grid-template-columns:repeat(8,20px);gap:8px}
  .chip{
    width:20px;height:20px;border-radius:4px;border:1px solid rgba(0,0,0,.15);
    cursor:pointer;
  }
  .hex-row{display:flex;gap:8px;margin-top:10px}
  .hex-row input{
    flex:1;border:1px solid #ccc;border-radius:6px;padding:6px 8px;font-size:13px;
  }
  .hex-row .btn.apply{white-space:nowrap}

  /* Keyboard safe area */
  @supports (padding: env(safe-area-inset-bottom)){
    footer{padding-bottom:calc(8px + env(safe-area-inset-bottom))}
  }
</style>
</head>
<body>
  <header>
    <div class="toolbar">
      <button id="bBold" class="btn small" title="Bold"><b>B</b></button>
      <button id="bItalic" class="btn small" title="Italic"><i>I</i></button>
      <button id="bUnderline" class="btn small" title="Underline"><u>U</u></button>

      <select id="fmtBlock" class="btn" title="Heading">
        <option value="">None</option>
        <option value="H1">H1</option>
        <option value="H2">H2</option>
        <option value="H3">H3</option>
        <option value="H4">H4</option>
        <option value="H5">H5</option>
        <option value="H6">H6</option>
        <option value="P">Normal</option>
      </select>

      <select id="fontSize" class="btn" title="Text size">
        <option value="">None</option>
        <option value="2">Small</option>
        <option value="3">Normal</option>
        <option value="4">Large</option>
        <option value="5">X-Large</option>
      </select>

      <!-- Foreground color -->
      <div class="color-wrap">
        <button id="fgBtn" class="btn color-toggle" title="Text color (F)">
          <span>F</span><span id="fgSwatch" class="swatch" style="background:#000000"></span>
        </button>
        <div id="fgPop" class="popover"></div>
      </div>

      <!-- Background color -->
      <div class="color-wrap">
        <button id="bgBtn" class="btn color-toggle" title="Background color (B)">
          <span>B</span><span id="bgSwatch" class="swatch" style="background:#ffffff"></span>
        </button>
        <div id="bgPop" class="popover"></div>
      </div>
    </div>
  </header>

  <div id="editor" contenteditable="true">Start typing here...</div>

  <footer>
    <div class="toolbar">
      <button id="bUL" class="btn">• List</button>
      <button id="bOL" class="btn">1. List</button>
      <button id="bChecklist" class="btn">☐ Checklist</button>
      <button id="bTable" class="btn">📊 Table</button>
    </div>
  </footer>

<script>
  // ---- Utilities ----
  const editor = document.getElementById('editor');
  document.execCommand('styleWithCSS', false, true);

  const $ = (id)=>document.getElementById(id);
  const buttons = {
    bold: $('bBold'), italic: $('bItalic'), underline: $('bUnderline'),
    ul: $('bUL'), ol: $('bOL'), checklist: $('bChecklist')
  };
  const selects = { block: $('fmtBlock'), size: $('fontSize') };
  const swatches = { fg: $('fgSwatch'), bg: $('bgSwatch') };
  const pops = { fg: $('fgPop'), bg: $('bgPop') };
  const fgBtn = $('fgBtn'), bgBtn = $('bgBtn');

  // Professional palettes (16+ each)
  const palette = [
    "#000000","#1F2937","#374151","#4B5563","#6B7280","#9CA3AF","#D1D5DB","#FFFFFF",
    "#0D47A1","#1565C0","#1E88E5","#42A5F5",
    "#0F766E","#10B981",
    "#B45309","#F59E0B",
    "#B91C1C","#EF4444",
    "#7C3AED","#8B5CF6"
  ];
  const bgPalette = [
    "#FFFFFF","#F9FAFB","#F3F4F6","#E5E7EB","#E3F2FD","#F0F9FF","#ECFDF5","#FEF3C7",
    "#FEE2E2","#F3E8FF","#FFF7ED","#FAF5FF","#FFF1F2","#F4F4F5","#F5F5F5","#FFF"
  ];

  function buildPopover(el, colors, onPick){
    const grid = document.createElement('div'); grid.className='grid';
    colors.forEach(c=>{
      const chip=document.createElement('div');chip.className='chip';chip.style.background=c;
      chip.title=c;chip.addEventListener('click',()=>onPick(c)); grid.appendChild(chip);
    });
    const hex = document.createElement('div'); hex.className='hex-row';
    hex.innerHTML = '<input placeholder="#000000" maxlength="9"><button class="btn apply">Apply</button>';
    const input = hex.querySelector('input'); const apply = hex.querySelector('.apply');
    apply.addEventListener('click',()=>{
      let v=input.value.trim();
      if(!v) return;
      if(!v.startsWith('#')) v='#'+v;
      onPick(v);
    });
    el.appendChild(grid); el.appendChild(hex);
  }

  buildPopover(pops.fg, palette, (hex)=>{ applyColor('foreColor', hex); swatches.fg.style.background=hex; closePopovers(); });
  buildPopover(pops.bg, bgPalette, (hex)=>{ applyColor('hiliteColor', hex); swatches.bg.style.background=hex; closePopovers(); });

  function openPopover(pop){ closePopovers(); pop.classList.add('open'); }
  function closePopovers(){ Object.values(pops).forEach(p=>p.classList.remove('open')); }
  document.addEventListener('click', e=>{
    if(!e.target.closest('.color-wrap')) closePopovers();
  });

  fgBtn.addEventListener('click', e=>{ e.stopPropagation(); openPopover(pops.fg); });
  bgBtn.addEventListener('click', e=>{ e.stopPropagation(); openPopover(pops.bg); });

  function format(cmd, val=null){ document.execCommand(cmd,false,val); editor.focus(); }

  // Buttons
  buttons.bold.onclick=()=>format('bold');
  buttons.italic.onclick=()=>format('italic');
  buttons.underline.onclick=()=>format('underline');
  buttons.ul.onclick=()=>format('insertUnorderedList');
  buttons.ol.onclick=()=>format('insertOrderedList');

  // Size & block
  selects.block.onchange=function(){
    if(!this.value) return;
    format('formatBlock', this.value);
  };
  selects.size.onchange=function(){
    if(!this.value) return;
    format('fontSize', this.value);
  };

  // Table
  $('bTable').onclick=()=>{
    const cols=prompt('How many columns?'); const rows=prompt('How many rows?');
    if(!cols || !rows) return;
    let html='<table><tr>';
    for(let c=0;c<cols;c++) html+='<th>Header '+(c+1)+'</th>';
    html+='</tr>';
    for(let r=0;r<rows;r++){ html+='<tr>'; for(let c=0;c<cols;c++) html+='<td></td>'; html+='</tr>'; }
    html+='</table><br/>';
    document.execCommand('insertHTML', false, html);
  };

  // ---- Checklist logic ----
  function closest(tag, el){
    while(el && el.nodeType===1){
      if(el.tagName===tag) return el;
      el=el.parentElement;
    }
    return null;
  }
  function selectionLI(){
    const sel=window.getSelection(); if(!sel.rangeCount) return null;
    let n=sel.getRangeAt(0).startContainer;
    if(n.nodeType===3) n=n.parentElement;
    return closest('LI', n);
  }
  function convertListToChecklist(list){
    if(!list) return;
    const isChecklist = list.classList.contains('checklist');
    if(isChecklist) return; // already checklist
    const html = Array.from(list.children).map(li=>{
      const text = li.innerHTML;
      return `<li><input type="checkbox"> <span>${text}</span></li>`;
    }).join('');
    const ul = document.createElement('ul');
    ul.className='checklist';
    ul.innerHTML=html;
    list.replaceWith(ul);
  }

  $('bChecklist').onclick=()=>{
    // If in UL/OL – override to checklist
    const li = selectionLI();
    const list = li ? li.parentElement : null;
    if(list && (list.tagName==='UL' || list.tagName==='OL')){
      convertListToChecklist(list);
      updateToolbarState();
      return;
    }
    // Otherwise insert a new checklist item
    const markup = `<ul class="checklist"><li><input type="checkbox"> <span>Checklist item</span></li></ul>`;
    document.execCommand('insertHTML', false, markup);
    updateToolbarState();
  };

  // Only toggle when clicking the checkbox, not text
  editor.addEventListener('click', (e)=>{
    if(e.target.matches('ul.checklist input[type="checkbox"]')){
      // no extra behavior; just allow native toggle
    }
  });

  // ---- Color apply & persistence ----
  let lastFG = "#000000", lastBG = "#ffffff";
  function rgbToHex(rgb){
    if(!rgb) return null;
    if(rgb.startsWith('#')) return rgb;
    const m = rgb.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/i);
    if(!m) return null;
    const toHex=(n)=>('0'+parseInt(n,10).toString(16)).slice(-2);
    return '#'+toHex(m[1])+toHex(m[2])+toHex(m[3]);
  }
  function queryColor(cmd){
    try{ return rgbToHex(document.queryCommandValue(cmd)); }catch{ return null; }
  }
  function applyColor(cmd, hex){
    document.execCommand(cmd,false,hex);
    if(cmd==='foreColor'){ lastFG=hex; swatches.fg.style.background=hex; }
    if(cmd==='hiliteColor'){ lastBG=hex; swatches.bg.style.background=hex; }
    updateToolbarState();
  }

  // Persist on Enter
  editor.addEventListener('keydown', (e)=>{
    if(e.key==='Enter'){
      // Delay to let the browser insert the new block, then re-apply
      setTimeout(()=>{
        const sel = window.getSelection();
        if(!sel.rangeCount) return;
        const range = sel.getRangeAt(0);
        if(range.collapsed){
          if(lastFG) document.execCommand('foreColor', false, lastFG);
          if(lastBG) document.execCommand('hiliteColor', false, lastBG);
        }
      },0);
    }
  });

  // ---- Toolbar state reflection ----
  function setActive(btn, on){ btn.classList.toggle('active', !!on); }
  function updateSelect(select, value){
    if(!select) return;
    const v = (value||'').toString().toUpperCase();
    for(const opt of select.options){
      if((opt.value||'').toString().toUpperCase()===v){ select.value=opt.value; return; }
    }
    select.value='';
  }
  function inChecklist(){
    const li = selectionLI(); return !!(li && li.parentElement && li.parentElement.classList.contains('checklist'));
  }
  function inList(tag){
    const li = selectionLI(); const list = li? li.parentElement : null;
    return !!(list && list.tagName===tag);
  }
  function currentBlockTag(){
    try{
      const v = document.queryCommandValue('formatBlock'); // e.g., "p", "h1" or "<p>"
      if(!v) return '';
      return v.replace(/[<>]/g,'').toUpperCase();
    }catch{ return ''; }
  }

  function updateToolbarState(){
    setActive(buttons.bold, document.queryCommandState('bold'));
    setActive(buttons.italic, document.queryCommandState('italic'));
    setActive(buttons.underline, document.queryCommandState('underline'));

    // Lists
    setActive(buttons.ul, inList('UL') && !inChecklist());
    setActive(buttons.ol, inList('OL'));
    setActive(buttons.checklist, inChecklist());

    // Block + size
    updateSelect(selects.block, currentBlockTag());
    updateSelect(selects.size, document.queryCommandValue('fontSize'));

    // Colors
    const fg = queryColor('foreColor') || lastFG;
    const bg = queryColor('hiliteColor') || lastBG;
    if(fg){ swatches.fg.style.background = fg; lastFG=fg; }
    if(bg){ swatches.bg.style.background = bg; lastBG=bg; }
  }

  document.addEventListener('selectionchange', ()=>{ if(document.activeElement===editor || editor.contains(document.activeElement)){ updateToolbarState(); } });
  editor.addEventListener('keyup', updateToolbarState);
  editor.addEventListener('mouseup', updateToolbarState);
  editor.addEventListener('input', updateToolbarState);

  // Focus the editor initially
  editor.focus();

  // ---- Keyboard lift (Android-friendly) ----
  (function keyboardLift(){
    const footer = document.querySelector('footer');
    if(window.visualViewport){
      const adjust=()=>{
        const vv = window.visualViewport;
        const kb = Math.max(0, (window.innerHeight - vv.height - vv.offsetTop));
        footer.style.position='fixed';
        footer.style.left='0'; footer.style.right='0';
        footer.style.bottom = kb+'px';
      };
      visualViewport.addEventListener('resize', adjust);
      visualViewport.addEventListener('scroll', adjust);
      window.addEventListener('resize', adjust);
      adjust();
    }
  })();
</script>
</body>
</html>
""".trimIndent()


        web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        return view
    }
}

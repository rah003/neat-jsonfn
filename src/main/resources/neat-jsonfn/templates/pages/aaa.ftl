[#assign page = cmsfn.page(content)]

[#if !(ctx.getParameter("workspace")?has_content)]
    Use parameter 'workspace' and optionally 'path' in url to get json of content you want. e.g.: ${ctx.contextPath}${page.@path}.js?workspace=tours
[#else]
    [#assign workspace = ctx.getParameter("workspace")]
    [#assign json = jsonfn.fromChildNodesOf(workspace).addAll().down(5).print()]
    ${json}
[/#if]

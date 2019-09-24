//Config
const server_domain = "olap.alexscode.com";

// Activate tooltips
$(function () {
    $('[data-toggle="tooltip"]').tooltip()
});

gTableList = [];


function local_demo_show() {
    var div = document.getElementById("local-demo-div");
    div.classList.remove("d-none");
}

function remote_load_show() {
    var div = document.getElementById("remote-file");
    div.classList.remove("d-none");
}

function submit_file() {
    const selectedFile = document.getElementById("input").files[0];
    var reader = new FileReader();
    reader.onload = function (e) {
        loadData(e.target.result);
    }
    reader.readAsText(selectedFile);
}

function submit_code() {
    const code = document.getElementById("input_id").value;
    fileRequest(code, loadData, console.log)
}


function getDepth(tree) {
    var i = 0;
    while (tree.children && tree.children.length) {
        i++;
        tree = tree.children[0];
    }
    return i;
}

function createTableHeaderCell(scope, rowspan, colspan, data) {
    var th = document.createElement("th");
    th.classList = "bg-light";
    if (scope) th.scope = scope;
    if (rowspan) th.rowSpan = rowspan;
    if (colspan) th.colSpan = colspan;
    if (data) th.innerText = data;
    return th;
}

function buildHeaderList(header_tree, axis_name, remove_root = false) {
    if (axis_name == "column") {
        if (remove_root) var res = [];
        else var res = [[{ "name": header_tree.name, "span": header_tree.span }]];
        if (header_tree.children) {
            var child_res = []
            for (let i = 0; i < header_tree.children.length; i++) {
                child_res.push(buildHeaderList(header_tree.children[i], "column"))
            }
            var tail = [];
            let i = 0;
            do {
                var child_list = [];
                for (let j = 0; j < child_res.length; j++) {
                    if (child_res[j][i]) child_list.push(child_res[j][i]);
                }
                if (child_list.length > 0) {
                    tail.push([]);
                    for (let j = 0; j < child_list.length; j++) {
                        tail[i] = tail[i].concat(child_list[j]);
                    }
                    i++;
                }
            } while (child_list.length > 0);
            res = res.concat(tail);
            return res;
        }
        return res;

    } else if (axis_name == "row") {
        if (remove_root) var res = [[]];
        else var res = [[{ "name": header_tree.name, "span": header_tree.span }]]
        if (header_tree.children) {
            if (header_tree.children.length > 0) {
                for (let i = 0; i < header_tree.children.length; i++) {
                    if (i == 0) {
                        var child_res = buildHeaderList(header_tree.children[i], "row");
                        res[0] = res[0].concat(child_res[0]);
                        if (child_res.length > 1) {
                            res = res.concat(child_res.slice(1));
                        }
                    } else {
                        res = res.concat(buildHeaderList(header_tree.children[i], "row"));
                    }
                }
                return res;
            }
        }
        return res;

    } else console.error("build_header_list method error: unknow axis_name " + axis_name);
}

function isPositionInList(position, list) {
    if (!list || !position) return false;
    for (let i = 0; i < list.length; i++) {
        const p = list[i];
        if (position.length != p.length) continue;
        var hasBroke = false;
        for (let j = 0; j < p.length; j++) {
            if (position[j] != p[j]) {
                hasBroke = true;
                break;
            };
        }
        if (hasBroke) continue;
        return true;
    }
    return false;
}

function changeTableState(id, state) {
    var table = gTableList[id];
    if (state != table.state) {
        var cells = table.cells;
        switch (state) {
            case "data":
                for (var i = 0; i < cells.length; i++) {
                    for (var j = 0; j < cells[i].length; j++) {
                        if (castorTableList[id]["data"][i][j] !== null) {
                            cells[i][j].textContent = +castorTableList[id]["data"][i][j].toFixed(2)
                            var classList = cells[i][j].classList;
                            if (classList.length == 2) {
                                classList.remove(classList[classList.length - 1])
                                classList.add("text-primary")
                            }
                        }
                    }
                }
                table.state = state;
                break;
            case "significance":
                for (var i = 0; i < cells.length; i++) {
                    for (var j = 0; j < cells[i].length; j++) {
                        if (castorTableList[id]["significanceScores"][i][j] !== null) {
                            cells[i][j].textContent = +castorTableList[id]["significanceScores"][i][j].toFixed(3)
                            var classList = cells[i][j].classList;
                            if (classList.length == 2) {
                                classList.remove(classList[classList.length - 1])
                                classList.add("text-success")
                            }
                        }
                    }
                }
                table.state = state;
                break;
            case "surprise":
                for (var i = 0; i < cells.length; i++) {
                    for (var j = 0; j < cells[i].length; j++) {
                        if (castorTableList[id]["surpriseScores"][i][j] !== null) {
                            cells[i][j].textContent = +castorTableList[id]["surpriseScores"][i][j].toFixed(3)
                            var classList = cells[i][j].classList;
                            if (classList.length == 2) {
                                classList.remove(classList[classList.length - 1])
                                classList.add("text-danger")
                            }
                        }
                    }
                }
                table.state = state;
                break;
            default:
                console.error("state " + state + " is unknown");
                break;
        }
    }
}

function switchColor(id) {
    var input = document.getElementById("section" + id + "-switch");
    var cells = gTableList[id].cells;
    var clusters = castorTableList[id].clusters;
    if (input.checked) {
        for (var row = 0; row < cells.length; row++) {
            for (var col = 0; col < cells[row].length; col++) {
                cell = cells[row][col]
                if (cell) cell.classList += " cluster-bg-" + clusters[row][col];
            }
        }
    } else {
        for (var row = 0; row < cells.length; row++) {
            for (var col = 0; col < cells[row].length; col++) {
                cell = cells[row][col]
                if (cell) cell.classList.remove("cluster-bg-" + clusters[row][col]);
            }
        }
    }
}

function fallbackCopyTextToClipboard(text) {
    var textArea = document.createElement("textarea");
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        console.log('Fallback: Copying text command was ' + msg);
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
    }

    document.body.removeChild(textArea);
}

function copyTextToClipboard(text) {
    if (!navigator.clipboard) {
        fallbackCopyTextToClipboard(text);
    } else {
        navigator.clipboard.writeText(text).then(function () {
            console.log('Async: Copying to clipboard was successful!');
        }, function (err) {
            console.error('Async: Could not copy text: ', err);
        });
    }
}

function fileRequest(code, callback, errorCallback) {
    // construct server url for API request
    const url = "http://" + server_domain + "/ressources/data/" + code + ".json";
    let xhr = new XMLHttpRequest();

    // modify callback to be executed when request completes
    function internCallback() {
        if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
            callback(xhr.responseText);
        }
        // if request fails (eg: network error)
        else if (xhr.readyState === 4 && typeof errorCallback !== 'undefined') {
            errorCallback(xhr.responseText, xhr.status);
        }
    }
    xhr.open('GET', url);
    //xhr.setRequestHeader("Access-Control-Allow-Origin", "*");

    // function to be called on state change
    xhr.onreadystatechange = internCallback;
    // send request
    xhr.send();
}

function loadData(raw){
    castorTableList = JSON.parse(raw).castorTableList;
    var castorTableListDiv = document.getElementById("castor_table_list");
    while (castorTableListDiv.lastChild) {
        castorTableListDiv.removeChild(castorTableListDiv.lastChild);
    }
    gTableList = [];

    // For each query result
    for (let id = 0; id < castorTableList.length; id++) {
        const result = castorTableList[id];

        var section = document.createElement("section"); // Create section
        section.classList = "container-fluid mt-5";
        section.id = "section" + id;
        section.appendChild(document.createElement("hr"));

        var b = document.createElement("b"); // Build id label
        b.classList = "mr-3";
        b.textContent = "Query " + id;//id;
        section.appendChild(b);

        // Add comments
        if (result.explanation) {
            let explanation = document.createElement("div");
            explanation.textContent = result.explanation;
            section.appendChild(explanation);
        }

        // Build left group
        var leftButtonGroupDiv = document.createElement("div");
        leftButtonGroupDiv.classList = "btn-group btn-group-toggle";
        leftButtonGroupDiv.setAttribute("data-toggle", "buttons");
        var dataButton = document.createElement("label"); // Data button
        dataButton.classList = "btn btn-outline-primary active";
        dataButton.textContent = "Data";
        dataButton.onclick = function () { changeTableState(id, "data"); };
        var dataInput = document.createElement("input");
        dataInput.type = "radio";
        dataInput.autocomplete = "off";
        dataInput.checked = true;
        dataButton.appendChild(dataInput);
        var significanceButton = document.createElement("label"); // Significance Button
        significanceButton.classList = "btn btn-outline-success";
        significanceButton.textContent = "Significance";
        significanceButton.onclick = function () { changeTableState(id, "significance"); };
        var significanceInput = document.createElement("input");
        significanceInput.type = "radio";
        significanceInput.autocomplete = "off";
        significanceButton.appendChild(significanceInput);
        var surpriseButton = document.createElement("label"); // Surprise Button
        surpriseButton.classList = "btn btn-outline-danger";
        surpriseButton.textContent = "Surprise";
        surpriseButton.onclick = function () { changeTableState(id, "surprise"); };
        var surpriseInput = document.createElement("input");
        surpriseInput.type = "radio";
        surpriseInput.autocomplete = "off";
        surpriseButton.appendChild(surpriseInput);
        var switchDiv = document.createElement("div");
        switchDiv.classList = "custom-control custom-switch custom-control-inline ml-3";
        var switchInput = document.createElement("input");
        switchInput.type = "checkbox";
        switchInput.classList = "custom-control-input";
        switchInput.id = "section" + id + "-switch";
        switchInput.onclick = function () { switchColor(id); }
        var switchLabel = document.createElement("label");
        switchLabel.classList = "custom-control-label";
        switchLabel.setAttribute("for", "section" + id + "-switch");
        switchLabel.textContent = "Show clusters";
        switchDiv.appendChild(switchInput);
        switchDiv.appendChild(switchLabel);
        leftButtonGroupDiv.appendChild(dataButton)
        leftButtonGroupDiv.appendChild(significanceButton)
        leftButtonGroupDiv.appendChild(surpriseButton)

        section.appendChild(leftButtonGroupDiv);
        section.appendChild(switchDiv);


        // Build right button group
        var rightGroupDiv = document.createElement("div");
        rightGroupDiv.classList = "float-right";
        var copyButton = document.createElement("button");
        copyButton.classList = "btn btn-secondary";
        copyButton.setAttribute("data-toggle", "tooltip");
        copyButton.setAttribute("data-placement", "left");
        copyButton.title = "Copy query to clipboard";
        copyButton.onclick = function () { copyTextToClipboard(result.query); };
        var span = document.createElement("span");
        span.classList = "oi oi-clipboard";
        copyButton.appendChild(span);
        rightGroupDiv.appendChild(copyButton);
        section.appendChild(rightGroupDiv);

        var tableObj = {
            state: "data"
        };
        // Build table
        var table = document.createElement("table");
        table.classList = "table table-sm table-bordered my-3";
        var tbody = document.createElement("tbody");
        var rowHeaders = buildHeaderList(result["rowHeaders"], "row", true);
        var columnHeaders = buildHeaderList(result["columnHeaders"], "column", true);
        var data = result["data"];
        var highlightedPositionList = result["highlightedCellPositions"]; // FIXME: Normalize name

        for (let i = 0; i < columnHeaders.length; i++) { // Build column headers
            var tr = document.createElement("tr");
            if (!i) {
                var th = createTableHeaderCell("col", getDepth(result["columnHeaders"]), getDepth(result["rowHeaders"]), "___");
                tr.appendChild(th);
            }
            for (let j = 0; j < columnHeaders[i].length; j++) {
                const header = columnHeaders[i][j];
                var th = createTableHeaderCell("col", 0, header.span, header.name);
                tr.appendChild(th);
            }
            tbody.appendChild(tr);
        }
        var cells = []
        for (let i = 0; i < data.length; i++) { // Build row headers and data
            var tr = document.createElement("tr");
            // Row headers
            try {
                for (let j = 0; j < rowHeaders[i].length; j++) {
                    const header = rowHeaders[i][j];
                    var th = createTableHeaderCell("row", header.span, 0, header.name);
                    tr.appendChild(th);
                }
            }catch (e) {
                console.log(e);
            }
            var cellRow = []
            for (let j = 0; j < data[i].length; j++) { // Data
                var td = document.createElement("td");
                cellRow.push(td);
                if (data[i][j]) {
                    td.textContent = +data[i][j].toFixed(2);
                    if (highlightedPositionList && isPositionInList([i, j], highlightedPositionList)) {
                        td.classList = "text-bold text-primary";
                    }
                }
                tr.appendChild(td);
            }
            tbody.append(tr);
            cells.push(cellRow);

        }
        table.appendChild(tbody);
        section.appendChild(table);
        tableObj.cells = cells;
        gTableList.push(tableObj);

        castorTableListDiv.appendChild(section);
    }
    //Activate tootips
    $('[data-toggle="tooltip"]').tooltip()
}

const decodeJwt = (token) => {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(escape(window.atob(base64))));
};

const obj = {
    textarea: document.getElementById("t"),
    log(newText) {
        this._text = newText + "\n" + this._text;
        this.textarea.textContent = this._text;
    },
    _text: ""
}

obj.log("hello world")
const liffId = "1656158895-Rygo23DL"

obj.log(`[global] before data const: `);
obj.log(`[global] before function initializeLiff`);

function initializeLiff(myLiffId) {
    liff
        .init({
            liffId: myLiffId
        })
        .then(() => {
            // start to use LIFF's api
            obj.log(`[liff init]`)
            const idTokenJwt = liff.getIDToken();
            const decoded = decodeJwt(idTokenJwt);
            obj.log(`[liff init] decoded: ${decoded}`)
            const idToken = liff.getDecodedIDToken();

            document.getElementById("decoded").textContent = JSON.stringify(idToken);
            document.getElementById("uid").textContent = idToken.sub
            document.getElementById("url").textContent = location.href;

            // 1. init に成功して、データを飛ばして mode to reg にできたら、spotify にリダイレクトする。
            // [fetch API]
            const code = urlParams.get("code")
            if (code) {
                decodedSendPost(decoded, code, obj);
            }


        })
        .catch((err) => {
            obj.log(`失敗しました: ${err}`)
        });
}
// 2. もし code が設定されてたら、 initializeLiff を 実行せず、 code を表示するだけ。
// if (false) {

const queryString = window.location.search;
obj.log(`[queryString] : ${queryString}`);
const urlParams = new URLSearchParams(queryString);

async function postDataString(url = '', data = {}) {
    // 既定のオプションには * が付いています
    const response = await fetch(url, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json'
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        body: (data) // 本文のデータ型は "Content-Type" ヘッダーと一致する必要があります
    })
    return response.json(); // レスポンスの JSON を解析
}

async function postData(url = '', data = {}) {
    // 既定のオプションには * が付いています
    const response = await fetch(url, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json'
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        body: JSON.stringify(data) // 本文のデータ型は "Content-Type" ヘッダーと一致する必要があります
    })
    return response.json(); // レスポンスの JSON を解析
}




const code = urlParams.get("code")


function decodedSendPost(decoded, code, obj) {
    // obj.log(`[ifcode_] code: ${code}`)
    const idTokenJwt = liff.getIDToken();
    obj.log(`before code`)
    // const decoded = decodeJwt(idTokenJwt);
    obj.log(`[decoded] ${JSON.stringify(decoded)}`)
    obj.log(`after decode code ${decoded}`)
    const data = {
        ...decoded,
        code: code
    }

    obj.log(`[decoded data] ${JSON.stringify(data)}`)


    // fetch("/post", {
    fetch("/test/post", {
        method: "POST",
        headers: {
            "Content-Type": 'application/json'
        },
        body: JSON.stringify(data)
    }).then(e => {
        const js = e.json()
        obj.log(`[post fetch] 正常終了: js log: ${JSON.stringify(js)}`)
    }).catch(e => {
        const ej = JSON.stringify(e);
        obj.log(`[fetch error] e: ${e}, ej: ${ej}`);
    })
}


if (code) {
    initializeLiff(liffId);
} else {
    obj.log(`[ifcode_] no code.. 🥺`)
    initializeLiff(liffId);
    obj.log(`[global] before initializeLiff`);
    obj.log(`[global] after  initializeLiff`);
}
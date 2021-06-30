const decodeJwt = (token) => {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(escape(window.atob(base64))));
};


const liffId = "1656158895-Rygo23DL"
function initializeLiff(myLiffId) {
    liff
        .init({
            liffId: myLiffId
        })
        .then(() => {
            // start to use LIFF's api
            const idTokenJwt = liff.getIDToken();
            const decoded = decodeJwt(idTokenJwt);
            const idToken = liff.getDecodedIDToken();
            document.getElementById("decoded").textContent = JSON.stringify(idToken);


            document.getElementById("uid").textContent = idToken.sub
            document.getElementById("url").textContent = location.href;

            // 1. init に成功して、データを飛ばして mode to reg にできたら、spotify にリダイレクトする。
            // [fetch API]


            // setTimeout(() => {
            //     location.href = "https://accounts.spotify.com/authorize?response_type=code&client_id=fa8da1cfbf9a40a4916307246b7f7222&scope=user-read-private%20user-read-email%20user-modify-playback-state%20user-read-recently-played%20user-top-read%20user-library-read%20user-read-playback-position%20user-library-modify%20user-follow-read%20playlist-modify-public%20user-read-playback-state%20user-read-currently-playing&redirect_uri=https%3A%2F%2Fliff%2Eline%2Eme%2F1656158895%2DRygo23DL"
            // }, 3000);

        })
        .catch((err) => {
            alert(`失敗しました: ${err}`)
            document.getElementById("liffAppContent").classList.add('hidden');
            document.getElementById("liffInitErrorMessage").classList.remove('hidden');
        });
}
// 2. もし code が設定されてたら、 initializeLiff を 実行せず、 code を表示するだけ。
initializeLiff(liffId)

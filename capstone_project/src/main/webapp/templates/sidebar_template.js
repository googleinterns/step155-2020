/**
 * Creates a custom Sidebar component. Can be called as a tag using:
 * <side-bar></side-bar>
 */
class SideBar extends HTMLElement {
  /** Sets the HTML for the sidebar element. */
  connectedCallback() {
    this.innerHTML = `
      <div class="collection">
        <a class="collection-item" href="/pages/maps.html">
          Explore More Schools
        </a>
        <a class="collection-item" href="/pages/comments.jsp">
          Create a Post
        </a>
      </div>`.trim();
  }
}

customElements.define('side-bar', SideBar);
